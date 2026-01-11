package cn.ahmisty.minecraft.xian.ffi.web.engine;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

public final class Engine {
    public final MemorySegment handle;

    public Engine(Config config) throws Throwable {
        handle = (MemorySegment) Abi.create.invokeExact(config.memory_segment);
    }

    public final void destroy() throws Throwable {
        Abi.destroy.invokeExact(handle);
    }

    public final void tick() throws Throwable {
        Abi.tick.invokeExact(handle);
    }

    public static final class Config {
        public final MemorySegment memory_segment;
        public final Arena arena;

        public Config(Arena arena) throws Throwable {
            this.arena = arena;
            memory_segment = arena.allocate(Abi.CONFIG);
            Abi.config_init.invokeExact(memory_segment);
        }
        public final Config set_glfw_shared_window(long glfw_shared_window) {
            memory_segment.set(ValueLayout.ADDRESS, Abi.OFFSET_CONFIG_GLFW_SHARED_WINDOW, MemorySegment.ofAddress(glfw_shared_window));
            return this;
        }
        public final Config set_embedder_glfw_api(MemorySegment glfw_api) {
            memory_segment.asSlice(Abi.OFFSET_CONFIG_GLFW_API, Abi.EMBEDDER_GLFW_API.byteSize()).copyFrom(glfw_api);
            return this;
        }
        public final Config set_resources_dir(String resources_dir) {
            memory_segment.set(ValueLayout.ADDRESS, Abi.OFFSET_CONFIG_RESOURCES_DIR, arena.allocateFrom(resources_dir, StandardCharsets.UTF_8));
            return this;
        }
        public final Config set_config_dir(String config_dir) {
            memory_segment.set(ValueLayout.ADDRESS, Abi.OFFSET_CONFIG_CONFIG_DIR, arena.allocateFrom(config_dir, StandardCharsets.UTF_8));
            return this;
        }
        public final Config set_default_width(int default_width) {
            memory_segment.set(ValueLayout.JAVA_INT, Abi.OFFSET_CONFIG_DEFAULT_WIDTH, default_width);
            return this;
        }
        public final Config set_default_height(int default_height) {
            memory_segment.set(ValueLayout.JAVA_INT, Abi.OFFSET_CONFIG_DEFAULT_HEIGHT, default_height);
            return this;
        }
        public final Config set_thread_pool_cap(int thread_pool_cap) {
            memory_segment.set(ValueLayout.JAVA_INT, Abi.OFFSET_CONFIG_THREAD_POOL_CAP, thread_pool_cap);
            return this;
        }
        public final Config set_engine_flags(int engine_flag) {
            memory_segment.set(ValueLayout.JAVA_INT, Abi.OFFSET_CONFIG_ENGINE_FLAGS, engine_flag);
            return this;
        }
    }
}
