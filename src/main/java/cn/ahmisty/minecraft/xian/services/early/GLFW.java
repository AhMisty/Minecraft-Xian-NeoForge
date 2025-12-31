package cn.ahmisty.minecraft.xian.services.early;

import com.google.auto.service.AutoService;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import net.neoforged.neoforgespi.locating.IOrderedProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.security.ProtectionDomain;

@AutoService({GraphicsBootstrapper.class})
public class GLFW implements GraphicsBootstrapper, IOrderedProvider {
    public static final String NAME = "仙";
    private static final String TITLE_PREFIX = NAME + ": ";
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("GLFW");
    private static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    private static volatile boolean HOOK_INSTALLED = false;
    private static final Object TRANSFORM_LOCK = new Object();
    private static volatile GlfwAsmTransformer TRANSFORMER = null;
    private static volatile boolean SELF_ATTACH_ATTEMPTED = false;

    @Override
    public String name () {return NAME;}

    @Override
    public int getPriority() {return Integer.MAX_VALUE;}

    @Override
    public void bootstrap(String[] arguments) {
        if (HOOK_INSTALLED) return;
        Instrumentation instrumentation = obtainInstrumentationOrNull();
        if (instrumentation == null) {
            LOGGER.error(LOGGERMARKER, "Could not obtain Instrumentation; GLFW hooks will not be installed");
            return;
        }

        GlfwAsmTransformer transformer = ensureTransformerInstalledOrNull(instrumentation);
        if (transformer == null) return;

        Class<?> loadedGlfw = findLoadedClassOrNull(instrumentation, "org.lwjgl.glfw.GLFW");
        if (loadedGlfw == null) return;

        installHooksForAlreadyLoadedGlfw(instrumentation, loadedGlfw, transformer);
    }

    private static void markHooksInstalled(String mode) {
        if (HOOK_INSTALLED) return;
        HOOK_INSTALLED = true;
        LOGGER.info(LOGGERMARKER, "Installed GLFW hooks ({})", mode);
    }

    private static GlfwAsmTransformer ensureTransformerInstalledOrNull(Instrumentation instrumentation) {
        GlfwAsmTransformer transformer = TRANSFORMER;
        if (transformer != null) return transformer;

        synchronized (TRANSFORM_LOCK) {
            transformer = TRANSFORMER;
            if (transformer != null) return transformer;

            transformer = new GlfwAsmTransformer(instrumentation);
            try {
                instrumentation.addTransformer(transformer, instrumentation.isRetransformClassesSupported());
                TRANSFORMER = transformer;
                return transformer;
            } catch (Throwable error) {
                LOGGER.error(LOGGERMARKER, "Failed to install GLFW ClassFileTransformer", error);
                return null;
            }
        }
    }

    private static void installHooksForAlreadyLoadedGlfw(Instrumentation instrumentation, Class<?> loadedGlfw, GlfwAsmTransformer transformer) {
        if (instrumentation.isRetransformClassesSupported()) {
            try {
                instrumentation.retransformClasses(loadedGlfw);
                markHooksInstalled("retransform");
                removeTransformerQuietly(instrumentation, transformer);
            } catch (UnmodifiableClassException error) {
                LOGGER.error(LOGGERMARKER, "Failed to hook org.lwjgl.glfw.GLFW", error);
            } catch (Throwable error) {
                LOGGER.error(LOGGERMARKER, "Unexpected error while hooking org.lwjgl.glfw.GLFW", error);
            }
            return;
        }

        if (instrumentation.isRedefineClassesSupported()) {
            try {
                byte[] originalBytes = readClassBytesOrNull(loadedGlfw, "org/lwjgl/glfw/GLFW.class");
                if (originalBytes == null) {
                    LOGGER.error(LOGGERMARKER, "org.lwjgl.glfw.GLFW is already loaded, but its class bytes could not be read; cannot redefine");
                    return;
                }
                byte[] transformedBytes = transformer.transform(
                        loadedGlfw.getModule(),
                        loadedGlfw.getClassLoader(),
                        "org/lwjgl/glfw/GLFW",
                        loadedGlfw,
                        loadedGlfw.getProtectionDomain(),
                        originalBytes
                );
                if (transformedBytes == null) {
                    LOGGER.error(LOGGERMARKER, "org.lwjgl.glfw.GLFW redefine failed: transformer returned null");
                    return;
                }
                instrumentation.redefineClasses(new java.lang.instrument.ClassDefinition(loadedGlfw, transformedBytes));
                markHooksInstalled("redefine");
                removeTransformerQuietly(instrumentation, transformer);
            } catch (Throwable error) {
                LOGGER.error(LOGGERMARKER, "Failed to redefine org.lwjgl.glfw.GLFW", error);
            }
            return;
        }

        LOGGER.error(LOGGERMARKER, "org.lwjgl.glfw.GLFW was already loaded, and the JVM/agent supports neither retransformation nor redefinition; cannot hook");
    }

    private static void removeTransformerQuietly(Instrumentation instrumentation, ClassFileTransformer transformer) {
        try {
            instrumentation.removeTransformer(transformer);
        } catch (Throwable ignored) {
            // ignore
        }
    }

    private static Instrumentation obtainInstrumentationOrNull() {
        Instrumentation instrumentation = getInstrumentationFromFmlDevAgentOrNull();
        if (instrumentation != null) return instrumentation;

        trySelfAttachFmlDevAgentOnce();
        return getInstrumentationFromFmlDevAgentOrNull();
    }

    private static Instrumentation getInstrumentationFromFmlDevAgentOrNull() {
        try {
            Class<?> devAgent = Class.forName("net.neoforged.fml.startup.DevAgent", false, ClassLoader.getSystemClassLoader());
            Field instrumentationField = devAgent.getDeclaredField("instrumentation");
            instrumentationField.setAccessible(true);
            Object value = instrumentationField.get(null);
            if (value instanceof Instrumentation inst) {
                return inst;
            }
        } catch (Throwable ignored) {
            // ignore
        }

        return null;
    }

    private static void trySelfAttachFmlDevAgentOnce() {
        if (SELF_ATTACH_ATTEMPTED) return;
        SELF_ATTACH_ATTEMPTED = true;

        String javaCommand = ProcessHandle.current().info().command().orElse(null);
        if (javaCommand == null || javaCommand.isBlank()) return;

        String classpathItem = getFmlLoaderClasspathItemOrNull();
        if (classpathItem == null || classpathItem.isBlank()) return;

        try {
            Process process = new ProcessBuilder(
                    javaCommand,
                    "--add-modules", "jdk.attach",
                    "-cp", classpathItem,
                    "net.neoforged.fml.startup.SelfAttach",
                    "net.neoforged.fml.startup.DevAgent"
            )
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            process.getOutputStream().close();
            process.waitFor();
        } catch (Throwable ignored) {
            // ignore
        }
    }

    private static String getFmlLoaderClasspathItemOrNull() {
        try {
            Class<?> devAgent = Class.forName("net.neoforged.fml.startup.DevAgent", false, ClassLoader.getSystemClassLoader());
            var codeSource = devAgent.getProtectionDomain().getCodeSource();
            if (codeSource == null || codeSource.getLocation() == null) return null;
            try {
                return Path.of(codeSource.getLocation().toURI()).toString();
            } catch (URISyntaxException error) {
                return codeSource.getLocation().toString();
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Class<?> findLoadedClassOrNull(Instrumentation instrumentation, String binaryName) {
        try {
            for (Class<?> loaded : instrumentation.getAllLoadedClasses()) {
                if (loaded == null) continue;
                if (binaryName.equals(loaded.getName())) return loaded;
            }
        } catch (Throwable ignored) {
            // ignore
        }
        return null;
    }

    private static byte[] readClassBytesOrNull(Class<?> clazz, String classResourcePath) {
        try (InputStream in = clazz.getClassLoader() != null
                ? clazz.getClassLoader().getResourceAsStream(classResourcePath)
                : ClassLoader.getSystemResourceAsStream(classResourcePath)) {
            if (in == null) return null;
            return in.readAllBytes();
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static CharSequence ensureTitlePrefix(CharSequence title) {
        if (title == null) return null;
        String titleString = title.toString();
        if (titleString.startsWith(TITLE_PREFIX)) return title;
        return TITLE_PREFIX + titleString;
    }

    private static final class GlfwAsmTransformer implements ClassFileTransformer {
        private static final String TARGET_CLASS_INTERNAL_NAME = "org/lwjgl/glfw/GLFW";
        private static final String THIS_CLASS_INTERNAL_NAME = "cn/ahmisty/minecraft/xian/services/early/GLFW";
        private static final String ICON_PROCESSOR_INTERNAL_NAME = THIS_CLASS_INTERNAL_NAME + "$IconProcessor";

        private final Instrumentation instrumentation;

        private GlfwAsmTransformer(Instrumentation instrumentation) {
            this.instrumentation = instrumentation;
        }

        @Override
        public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            String effectiveClassName = className;
            if (effectiveClassName == null && classBeingRedefined != null) {
                effectiveClassName = classBeingRedefined.getName().replace('.', '/');
            }
            if (!TARGET_CLASS_INTERNAL_NAME.equals(effectiveClassName)) return null;

            try {
                ClassReader reader = new ClassReader(classfileBuffer);
                ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                ClassVisitor visitor = new GlfwClassVisitor(writer);
                reader.accept(visitor, 0);
                byte[] out = writer.toByteArray();
                if (classBeingRedefined == null) {
                    markHooksInstalled("load-time");
                    if (instrumentation != null) {
                        removeTransformerQuietly(instrumentation, this);
                    }
                }
                return out;
            } catch (Throwable error) {
                LOGGER.error(LOGGERMARKER, "Failed to ASM-transform {}", effectiveClassName, error);
                return null;
            }
        }

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            return transform(null, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        }

        private static final class GlfwClassVisitor extends ClassVisitor {
            private GlfwClassVisitor(ClassVisitor classVisitor) {
                super(Opcodes.ASM9, classVisitor);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (mv == null) return null;
                if ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) return mv;

                boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
                if (!isStatic) return mv;

                if ("glfwCreateWindow".equals(name) && "(IILjava/lang/CharSequence;JJ)J".equals(descriptor)) {
                    return new InjectAtEntryMethodVisitor(mv, InjectAtEntryMethodVisitor.InjectionType.PREFIX_TITLE, 2);
                }
                if ("glfwSetWindowTitle".equals(name) && "(JLjava/lang/CharSequence;)V".equals(descriptor)) {
                    return new InjectAtEntryMethodVisitor(mv, InjectAtEntryMethodVisitor.InjectionType.PREFIX_TITLE, 2);
                }
                if ("glfwSetWindowIcon".equals(name) && "(JLorg/lwjgl/glfw/GLFWImage$Buffer;)V".equals(descriptor)) {
                    return new InjectAtEntryMethodVisitor(mv, InjectAtEntryMethodVisitor.InjectionType.OVERLAY_ICON, 2);
                }

                return mv;
            }
        }

        private static final class InjectAtEntryMethodVisitor extends MethodVisitor {
            enum InjectionType {
                PREFIX_TITLE,
                OVERLAY_ICON
            }

            private final InjectionType injectionType;
            private final int localIndex;

            private InjectAtEntryMethodVisitor(MethodVisitor methodVisitor, InjectionType injectionType, int localIndex) {
                super(Opcodes.ASM9, methodVisitor);
                this.injectionType = injectionType;
                this.localIndex = localIndex;
            }

            @Override
            public void visitCode() {
                super.visitCode();

                switch (injectionType) {
                    case PREFIX_TITLE -> {
                        visitVarInsn(Opcodes.ALOAD, localIndex);
                        visitMethodInsn(Opcodes.INVOKESTATIC, THIS_CLASS_INTERNAL_NAME, "ensureTitlePrefix", "(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;", false);
                        visitVarInsn(Opcodes.ASTORE, localIndex);
                    }
                    case OVERLAY_ICON -> {
                        visitVarInsn(Opcodes.ALOAD, localIndex);
                        visitMethodInsn(Opcodes.INVOKESTATIC, ICON_PROCESSOR_INTERNAL_NAME, "overlayImageOnIcons", "(Lorg/lwjgl/glfw/GLFWImage$Buffer;)V", false);
                    }
                }
            }
        }

    }

    public static final class IconProcessor {
        private static final String OVERLAY_PNG_PATH = "/icon.png";

        public static void overlayImageOnIcons(GLFWImage.Buffer originalIcons) {
            ByteBuffer overlayImage = null;
            int overlayWidth, overlayHeight;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer rawPngData = loadResourceToByteBuffer(OVERLAY_PNG_PATH);
                if (rawPngData == null) {
                    LOGGER.error(LOGGERMARKER, "Could not find icon resource: {}", OVERLAY_PNG_PATH);
                    return;
                }
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);
                overlayImage = STBImage.stbi_load_from_memory(rawPngData, w, h, comp, 4);
                if (overlayImage == null) {
                    LOGGER.error(LOGGERMARKER, "Failed to load overlay image: {}", STBImage.stbi_failure_reason());
                    MemoryUtil.memFree(rawPngData);
                    return;
                }
                overlayWidth = w.get(0);
                overlayHeight = h.get(0);
                MemoryUtil.memFree(rawPngData);
            }
            try {
                for (GLFWImage icon : originalIcons) {
                    int baseW = icon.width();
                    int baseH = icon.height();
                    int capacity = baseW * baseH * 4;
                    ByteBuffer basePixels = icon.pixels(capacity);

                    int badgeW = baseW / 2;
                    int badgeH = baseH / 2;
                    if (badgeW < 1) badgeW = 1;
                    if (badgeH < 1) badgeH = 1;

                    int destX = baseW - badgeW;
                    int destY = baseH - badgeH;

                    ByteBuffer resizedOverlay = MemoryUtil.memAlloc(badgeW * badgeH * 4);
                    try {
                        STBImageResize.stbir_resize_uint8(
                                overlayImage, overlayWidth, overlayHeight, 0,
                                resizedOverlay, badgeW, badgeH, 0,
                                4 // RGBA
                        );

                        blendPixels(basePixels, baseW, resizedOverlay, badgeW, badgeH, destX, destY);

                    } finally {
                        MemoryUtil.memFree(resizedOverlay);
                    }
                }
            } finally {
                STBImage.stbi_image_free(overlayImage);
            }
        }

        private static ByteBuffer loadResourceToByteBuffer(String resource) {
            try (InputStream is = GLFW.class.getResourceAsStream(resource)) {
                if (is == null) return null;
                byte[] bytes = is.readAllBytes();
                ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                return buffer;
            } catch (IOException error) {
                LOGGER.error(LOGGERMARKER,"Failed to load Resource", error);
                return null;
            }
        }

        private static void blendPixels(ByteBuffer baseBuf, int baseW, ByteBuffer overlayBuf, int overW, int overH, int offX, int offY) {
            for (int y = 0; y < overH; y++) {
                for (int x = 0; x < overW; x++) {
                    int overIdx = (y * overW + x) * 4;

                    int baseY = offY + y;
                    int baseX = offX + x;
                    int baseIdx = (baseY * baseW + baseX) * 4;

                    float r2 = (overlayBuf.get(overIdx) & 0xFF) / 255.0f;
                    float g2 = (overlayBuf.get(overIdx + 1) & 0xFF) / 255.0f;
                    float b2 = (overlayBuf.get(overIdx + 2) & 0xFF) / 255.0f;
                    float a2 = (overlayBuf.get(overIdx + 3) & 0xFF) / 255.0f;

                    if (a2 <= 0.0f) continue;

                    float r1 = (baseBuf.get(baseIdx) & 0xFF) / 255.0f;
                    float g1 = (baseBuf.get(baseIdx + 1) & 0xFF) / 255.0f;
                    float b1 = (baseBuf.get(baseIdx + 2) & 0xFF) / 255.0f;
                    float a1 = (baseBuf.get(baseIdx + 3) & 0xFF) / 255.0f;

                    float outA = a2 + a1 * (1 - a2);
                    if (outA > 0) {
                        float outR = (r2 * a2 + r1 * a1 * (1 - a2)) / outA;
                        float outG = (g2 * a2 + g1 * a1 * (1 - a2)) / outA;
                        float outB = (b2 * a2 + b1 * a1 * (1 - a2)) / outA;

                        baseBuf.put(baseIdx, (byte) (outR * 255));
                        baseBuf.put(baseIdx + 1, (byte) (outG * 255));
                        baseBuf.put(baseIdx + 2, (byte) (outB * 255));
                        baseBuf.put(baseIdx + 3, (byte) (outA * 255));
                    }
                }
            }
        }
    }
}
