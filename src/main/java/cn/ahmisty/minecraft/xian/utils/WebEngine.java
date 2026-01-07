package cn.ahmisty.minecraft.xian.utils;

import cn.ahmisty.minecraft.xian.ffi.Strings;
import cn.ahmisty.minecraft.xian.ffi.Types;
import cn.ahmisty.minecraft.xian.ffi.Util;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

public final class WebEngine implements AutoCloseable {
    private final WebEngineAbi abi;
    private long handle;

    public WebEngine(Config config) {
        this(WebEngineAbi.shared(), config);
    }

    public WebEngine(WebEngineAbi abi, Config config) {
        this.abi = Objects.requireNonNull(abi, "abi");
        Objects.requireNonNull(config, "config");
        config.validate();

        long created;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cfg = WebEngineAbi.EngineConfig.allocate(arena);
            abi.engineConfigInit(cfg);

            cfg.set(Types.U64, WebEngineAbi.EngineConfig.GLFW_SHARED_WINDOW, config.glfwSharedWindow);

            cfg.set(Types.U64, WebEngineAbi.EngineConfig.GLFW_GET_PROC_ADDRESS, config.glfwGetProcAddress);
            cfg.set(Types.U64, WebEngineAbi.EngineConfig.GLFW_MAKE_CONTEXT_CURRENT, config.glfwMakeContextCurrent);
            cfg.set(Types.U64, WebEngineAbi.EngineConfig.GLFW_DEFAULT_WINDOW_HINTS, config.glfwDefaultWindowHints);
            cfg.set(Types.U64, WebEngineAbi.EngineConfig.GLFW_WINDOW_HINT, config.glfwWindowHint);
            cfg.set(Types.U64, WebEngineAbi.EngineConfig.GLFW_GET_WINDOW_ATTRIB, config.glfwGetWindowAttrib);
            cfg.set(Types.U64, WebEngineAbi.EngineConfig.GLFW_CREATE_WINDOW, config.glfwCreateWindow);
            cfg.set(Types.U64, WebEngineAbi.EngineConfig.GLFW_DESTROY_WINDOW, config.glfwDestroyWindow);

            cfg.set(Types.U32, WebEngineAbi.EngineConfig.DEFAULT_WIDTH, config.defaultWidth);
            cfg.set(Types.U32, WebEngineAbi.EngineConfig.DEFAULT_HEIGHT, config.defaultHeight);
            cfg.set(Types.U32, WebEngineAbi.EngineConfig.THREAD_POOL_CAP, config.threadPoolCap);
            cfg.set(Types.U32, WebEngineAbi.EngineConfig.ENGINE_FLAGS, config.engineFlags);

            long resourcesDir = Strings.utf8zOrNullIfBlankAddress(arena, config.resourcesDir);
            if (resourcesDir != 0L) {
                cfg.set(Types.U64, WebEngineAbi.EngineConfig.RESOURCES_DIR, resourcesDir);
            }
            long configDir = Strings.utf8zOrNullIfBlankAddress(arena, config.configDir);
            if (configDir != 0L) {
                cfg.set(Types.U64, WebEngineAbi.EngineConfig.CONFIG_DIR, configDir);
            }

            created = abi.engineCreate(cfg);
        }

        if (created == 0L) {
            throw new IllegalStateException("xian_web_engine_create returned NULL");
        }
        this.handle = created;
    }

    public WebEngineAbi abi() {
        return abi;
    }

    public long handle() {
        return handle;
    }

    public boolean isClosed() {
        return handle == 0L;
    }

    public void tick() {
        long engine = handle;
        if (engine == 0L) {
            return;
        }
        abi.engineTick(engine);
    }

    public WebEngineView createView(WebEngineView.Config config) {
        long engine = handle;
        if (engine == 0L) {
            throw new IllegalStateException("engine is closed");
        }
        return new WebEngineView(abi, engine, config);
    }

    public int acquireFrames(long views, long outViewIndices, long outFrames, int count) {
        return abi.viewsAcquireFrames(views, outViewIndices, outFrames, count);
    }

    public int acquireFrames(MemorySegment views, MemorySegment outViewIndices, MemorySegment outFrames, int count) {
        return acquireFrames(Util.address(views), Util.address(outViewIndices), Util.address(outFrames), count);
    }

    public int acquireFrames(WebEngineFrameBatch batch, int count) {
        if (batch == null) {
            return 0;
        }
        return acquireFrames(batch.viewsAddress(), batch.outViewIndicesAddress(), batch.outFramesAddress(), count);
    }

    public void releaseFrames(long views, long slots, long consumerFences, int count) {
        abi.viewsReleaseFrames(views, slots, consumerFences, count);
    }

    public void releaseFrames(MemorySegment views, MemorySegment slots, MemorySegment consumerFences, int count) {
        releaseFrames(Util.address(views), Util.address(slots), Util.address(consumerFences), count);
    }

    public void releaseFrames(WebEngineFrameBatch batch, int count) {
        if (batch == null) {
            return;
        }
        releaseFrames(batch.viewsAddress(), batch.slotsAddress(), batch.consumerFencesAddress(), count);
    }

    @Override
    public void close() {
        long engine = handle;
        if (engine == 0L) {
            return;
        }
        handle = 0L;
        abi.engineDestroy(engine);
    }

    public static final class Config {
        public long glfwSharedWindow;
        public long glfwGetProcAddress;
        public long glfwMakeContextCurrent;
        public long glfwDefaultWindowHints;
        public long glfwWindowHint;
        public long glfwGetWindowAttrib;
        public long glfwCreateWindow;
        public long glfwDestroyWindow;

        public int defaultWidth = 1;
        public int defaultHeight = 1;
        public int threadPoolCap = 0;
        public int engineFlags = 0;
        public String resourcesDir;
        public String configDir;

        public void validate() {
            requireNonZero(glfwSharedWindow, "glfwSharedWindow");
            requireNonZero(glfwGetProcAddress, "glfwGetProcAddress");
            requireNonZero(glfwMakeContextCurrent, "glfwMakeContextCurrent");
            requireNonZero(glfwDefaultWindowHints, "glfwDefaultWindowHints");
            requireNonZero(glfwWindowHint, "glfwWindowHint");
            requireNonZero(glfwGetWindowAttrib, "glfwGetWindowAttrib");
            requireNonZero(glfwCreateWindow, "glfwCreateWindow");
            requireNonZero(glfwDestroyWindow, "glfwDestroyWindow");
        }

        private static void requireNonZero(long value, String name) {
            if (value == 0L) {
                throw new IllegalArgumentException(name + " must be non-zero");
            }
        }
    }
}
