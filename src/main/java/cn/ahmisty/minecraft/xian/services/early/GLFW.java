package cn.ahmisty.minecraft.xian.services.early;

import com.google.auto.service.AutoService;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import net.neoforged.neoforgespi.locating.IOrderedProvider;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@AutoService({GraphicsBootstrapper.class})
public class GLFW implements GraphicsBootstrapper, IOrderedProvider {
    public static final String NAME = "Xian/GLFW";
    private static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    @Override
    public String name () {return NAME;}

    @Override
    public int getPriority() {return Integer.MAX_VALUE;}

    @Override
    public void bootstrap(String[] arguments) {
        ByteBuddyAgent.install();
        new ByteBuddy()
                .redefine(org.lwjgl.glfw.GLFW.class)
                .visit(Advice.to(glfwWindowHintStringAdvice.class).on(
                        ElementMatchers.named("glfwSetWindowTitle")
                                .and(ElementMatchers.takesArgument(1, CharSequence.class))
                ))
                .visit(Advice.to(GlfwCreateWindowAdvice.class).on(
                        ElementMatchers.named("glfwCreateWindow")
                                .and(ElementMatchers.takesArgument(2, CharSequence.class))
                ))
                .visit(Advice.to(GlfwSetWindowIconAdvice.class).on(
                        ElementMatchers.named("glfwSetWindowIcon")
                ))
                .visit(Advice.to(GlfwSetWindowTitleAdvice.class).on(
                        ElementMatchers.named("glfwSetWindowTitle")
                                .and(ElementMatchers.takesArgument(1, CharSequence.class))
                ))
                .visit(Advice.to(glfwSwapBuffersAdvice.class).on(
                        ElementMatchers.named("glfwSwapBuffers")
                ))
                .make()
                .load(org.lwjgl.glfw.GLFW.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }
    public static class glfwWindowHintStringAdvice {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(value = 1, readOnly = false) CharSequence value) {
            value = "Xian: " + value;
        }
    }
    public static class GlfwCreateWindowAdvice {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(value = 2, readOnly = false) CharSequence title) {
            title = "Xian: " + title;
        }
    }
    public static class GlfwSetWindowIconAdvice {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(0) long window, @Advice.Argument(value = 1, readOnly = false) GLFWImage.Buffer images) {
            IconProcessor.overlayImageOnIcons(images, "/icon.png");
        }
    }
    public static class GlfwSetWindowTitleAdvice {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(value = 1, readOnly = false) CharSequence title) {
            String prefix = "Xian: ";
            if (!title.toString().startsWith(prefix)) {
                title = prefix + title;
            }
        }
    }
    public static class glfwSwapBuffersAdvice {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(0) long window) {
        }
    }

    public static class IconProcessor {

        public static void overlayImageOnIcons(GLFWImage.Buffer originalIcons, String overlayPngPath) {
            ByteBuffer overlayImage = null;
            int overlayWidth, overlayHeight;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer rawPngData = loadResourceToByteBuffer(overlayPngPath);
                if (rawPngData == null) {
                    LOGGER.error("Could not find icon resource: {}", overlayPngPath);
                    return;
                }
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);
                overlayImage = STBImage.stbi_load_from_memory(rawPngData, w, h, comp, 4);
                if (overlayImage == null) {
                    LOGGER.error("Failed to load overlay image: {}", STBImage.stbi_failure_reason());
                    MemoryUtil.memFree(rawPngData);
                    return;
                }
                overlayWidth = w.get(0);
                overlayHeight = h.get(0);
                MemoryUtil.memFree(rawPngData);
            }
            try {
                // 2. 遍历 Minecraft 设置的所有图标尺寸
                for (GLFWImage icon : originalIcons) {
                    int baseW = icon.width();
                    int baseH = icon.height();
                    int capacity = baseW * baseH * 4;
                    ByteBuffer basePixels = icon.pixels(capacity);

                    // 设定右下角小图标的大小，例如原图标的 40% ~ 50%
                    int badgeW = baseW * 2 / 5;
                    int badgeH = baseH * 2 / 5;
                    // 确保至少是 1x1
                    if (badgeW < 1) badgeW = 1;
                    if (badgeH < 1) badgeH = 1;

                    // 计算放置位置：右下角
                    int destX = baseW - badgeW;
                    int destY = baseH - badgeH;

                    // 3. 缩放你的 icon.png 到目标大小
                    ByteBuffer resizedOverlay = MemoryUtil.memAlloc(badgeW * badgeH * 4);
                    try {
                        STBImageResize.stbir_resize_uint8(
                                overlayImage, overlayWidth, overlayHeight, 0,
                                resizedOverlay, badgeW, badgeH, 0,
                                4 // RGBA
                        );

                        // 4. 将缩放后的图标混合到原图标的右下角
                        blendPixels(basePixels, baseW, resizedOverlay, badgeW, badgeH, destX, destY);

                    } finally {
                        MemoryUtil.memFree(resizedOverlay);
                    }
                }
            } finally {
                if (overlayImage != null) {
                    STBImage.stbi_image_free(overlayImage);
                }
            }
        }

        // 读取资源文件到 Direct ByteBuffer
        private static ByteBuffer loadResourceToByteBuffer(String resource) {
            try (InputStream is = GLFW.class.getResourceAsStream(resource)) {
                if (is == null) return null;
                byte[] bytes = is.readAllBytes();
                ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                return buffer;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        // 像素混合 (Alpha Blending)
        private static void blendPixels(ByteBuffer baseBuf, int baseW, ByteBuffer overlayBuf, int overW, int overH, int offX, int offY) {
            for (int y = 0; y < overH; y++) {
                for (int x = 0; x < overW; x++) {
                    // 你的图标像素索引
                    int overIdx = (y * overW + x) * 4;

                    // 原始图标对应的像素索引
                    int baseY = offY + y;
                    int baseX = offX + x;
                    int baseIdx = (baseY * baseW + baseX) * 4;

                    // 获取 RGBA (转换无符号)
                    float r2 = (overlayBuf.get(overIdx) & 0xFF) / 255.0f;
                    float g2 = (overlayBuf.get(overIdx + 1) & 0xFF) / 255.0f;
                    float b2 = (overlayBuf.get(overIdx + 2) & 0xFF) / 255.0f;
                    float a2 = (overlayBuf.get(overIdx + 3) & 0xFF) / 255.0f;

                    // 如果你的图标这里完全透明，就跳过计算，节省性能
                    if (a2 <= 0.0f) continue;

                    float r1 = (baseBuf.get(baseIdx) & 0xFF) / 255.0f;
                    float g1 = (baseBuf.get(baseIdx + 1) & 0xFF) / 255.0f;
                    float b1 = (baseBuf.get(baseIdx + 2) & 0xFF) / 255.0f;
                    float a1 = (baseBuf.get(baseIdx + 3) & 0xFF) / 255.0f;

                    // 标准混合公式
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
