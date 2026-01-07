package cn.ahmisty.minecraft.xian.ffi.web.engine;

import cn.ahmisty.minecraft.xian.ffi.Types;
import net.minecraft.client.Minecraft;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

public class Engine implements AutoCloseable {
    public static final int FLAG_NO_PARK = 1 << 0;

    public static final int VIEW_FLAG_UNSAFE_NO_CONSUMER_FENCE = 1 << 0;
    public static final int VIEW_FLAG_INPUT_SINGLE_PRODUCER = 1 << 1;
    public static final int VIEW_FLAG_UNSAFE_NO_PRODUCER_FENCE = 1 << 2;

    public static final int INPUT_KIND_MOUSE_MOVE = 1;
    public static final int INPUT_KIND_MOUSE_BUTTON = 2;
    public static final int INPUT_KIND_WHEEL = 3;
    public static final int INPUT_KIND_KEY = 4;

    private static final long CONFIG_GLFW_SHARED_WINDOW_OFFSET =
            Abi.CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("glfw_shared_window"));
    private static final long CONFIG_GLFW_API_OFFSET =
            Abi.CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("glfw_api"));
    private static final long CONFIG_DEFAULT_WIDTH_OFFSET =
            Abi.CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("default_width"));
    private static final long CONFIG_DEFAULT_HEIGHT_OFFSET =
            Abi.CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("default_height"));
    private static final long CONFIG_THREAD_POOL_CAP_OFFSET =
            Abi.CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("thread_pool_cap"));
    private static final long CONFIG_ENGINE_FLAGS_OFFSET =
            Abi.CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("engine_flags"));
    private static final long CONFIG_RESOURCES_DIR_OFFSET =
            Abi.CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("resources_dir"));
    private static final long CONFIG_CONFIG_DIR_OFFSET =
            Abi.CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("config_dir"));

    private long handle;

    private static long utf8zOrNullIfBlankAddress(Arena arena, String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        return arena.allocateFrom(value, StandardCharsets.UTF_8).address();
    }

    public Engine(Config config) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cfg = arena.allocate(Abi.CONFIG);
            Abi.config_init.invokeExact(cfg.address());

            cfg.set(Types.U64, CONFIG_GLFW_SHARED_WINDOW_OFFSET, config.glfwSharedWindow);
            cfg.asSlice(CONFIG_GLFW_API_OFFSET, Abi.GLFW_API.byteSize())
                    .copyFrom(config.glfwApi == null ? Abi.DEFAULT_GLFW_API : config.glfwApi);
            cfg.set(Types.U32, CONFIG_DEFAULT_WIDTH_OFFSET, config.defaultWidth);
            cfg.set(Types.U32, CONFIG_DEFAULT_HEIGHT_OFFSET, config.defaultHeight);
            cfg.set(Types.U32, CONFIG_THREAD_POOL_CAP_OFFSET, config.threadPoolCap);
            cfg.set(Types.U32, CONFIG_ENGINE_FLAGS_OFFSET, config.engineFlags);
            cfg.set(
                    Types.U64,
                    CONFIG_RESOURCES_DIR_OFFSET,
                    utf8zOrNullIfBlankAddress(arena, config.resourcesDir)
            );
            cfg.set(
                    Types.U64,
                    CONFIG_CONFIG_DIR_OFFSET,
                    utf8zOrNullIfBlankAddress(arena, config.configDir)
            );

            handle = (long) Abi.create.invokeExact(cfg.address());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        if (handle == 0) {
            throw new IllegalStateException("xian_web_engine_create returned NULL");
        }
    }

    public long handle() {
        return handle;
    }

    public void tick() {
        try {
            Abi.tick.invokeExact(handle);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public View createView(View.Config config) {
        return new View(this, config);
    }

    public void destroy() {
        long ptr = handle;
        if (ptr == 0) {
            return;
        }
        handle = 0;
        try {
            Abi.destroy.invokeExact(ptr);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void close() {
        destroy();
    }

    public static final class Config {
        public long glfwSharedWindow = Minecraft.getInstance().getWindow().handle();
        public MemorySegment glfwApi = Abi.DEFAULT_GLFW_API;
        public int defaultWidth = 1;
        public int defaultHeight = 1;
        public int threadPoolCap = 0;
        public int engineFlags = 0;
        public String resourcesDir = null;
        public String configDir = null;

        public Config glfwSharedWindow(long glfwSharedWindow) {
            this.glfwSharedWindow = glfwSharedWindow;
            return this;
        }

        public Config glfwApi(MemorySegment glfwApi) {
            this.glfwApi = glfwApi;
            return this;
        }

        public Config defaultSize(int width, int height) {
            this.defaultWidth = width;
            this.defaultHeight = height;
            return this;
        }

        public Config threadPoolCap(int threadPoolCap) {
            this.threadPoolCap = threadPoolCap;
            return this;
        }

        public Config engineFlags(int engineFlags) {
            this.engineFlags = engineFlags;
            return this;
        }

        public Config resourcesDir(String resourcesDir) {
            this.resourcesDir = resourcesDir;
            return this;
        }

        public Config configDir(String configDir) {
            this.configDir = configDir;
            return this;
        }
    }
}
