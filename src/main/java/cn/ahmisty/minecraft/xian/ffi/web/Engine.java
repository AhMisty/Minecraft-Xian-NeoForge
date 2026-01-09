package cn.ahmisty.minecraft.xian.ffi.web;

import cn.ahmisty.minecraft.xian.ffi.Types;
import cn.ahmisty.minecraft.xian.ffi.web.engine.Abi;
import net.minecraft.client.Minecraft;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

public class Engine implements AutoCloseable {
    public static final int FLAG_NO_PARK = Abi.EngineFlag.NO_PARK;

    private long handle;

    public Engine(Config config) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cfg = arena.allocate(Abi.Struct.CONFIG);
            Abi.config_init.invokeExact(cfg.address());

            cfg.set(Types.U64, Abi.Offset.CONFIG.GLFW_SHARED_WINDOW, config.glfwSharedWindow);
            cfg.asSlice(Abi.Struct.OFFSET_CONFIG.CONFIG_GLFW_API_OFFSET, Abi.GLFW_API.byteSize())
                    .copyFrom(config.glfwApi == null ? Abi.DEFAULT_GLFW_API : config.glfwApi);
            cfg.set(Types.U32, CONFIG_DEFAULT_WIDTH_OFFSET, config.defaultWidth);
            cfg.set(Types.U32, CONFIG_DEFAULT_HEIGHT_OFFSET, config.defaultHeight);
            cfg.set(Types.U32, CONFIG_THREAD_POOL_CAP_OFFSET, config.threadPoolCap);
            cfg.set(Types.U32, CONFIG_ENGINE_FLAGS_OFFSET, config.engineFlags);
            cfg.set(
                    Types.U64,
                    CONFIG_RESOURCES_DIR_OFFSET,
                    arena.allocateFrom(config.resourcesDir, StandardCharsets.UTF_8).address()
            );
            cfg.set(
                    Types.U64,
                    CONFIG_CONFIG_DIR_OFFSET,
                    arena.allocateFrom(config.configDir, StandardCharsets.UTF_8).address()
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
