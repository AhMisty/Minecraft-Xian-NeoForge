package cn.ahmisty.minecraft.xian.ffi.web.engine;

import cn.ahmisty.minecraft.xian.ffi.Library;
import cn.ahmisty.minecraft.xian.ffi.Types;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public final class Abi {
    public static final Arena ARENA = Arena.global();
    private static final Library LIBRARY = new Library("xian_web_engine", ARENA);

    /**
     * Engine 创建 flags（对应 Rust 侧的 `engine_flags`）。
     */
    public static final class EngineFlag {
        /**
         * `XIAN_WEB_ENGINE_FLAG_NO_PARK`：Servo 线程 idle 时不 park（busy-spin）。
         * 低延迟但会显著增加空闲 CPU 占用。
         */
        public static final int NO_PARK = 1 << 0;

        private EngineFlag() {
        }
    }

    /**
     * View 创建 flags（对应 Rust 侧的 `view_flags`）。
     */
    public static final class ViewFlag {
        /**
         * `XIAN_WEB_ENGINE_VIEW_FLAG_UNSAFE_NO_CONSUMER_FENCE`：忽略 consumer fence（更快但不安全）。
         */
        public static final int UNSAFE_NO_CONSUMER_FENCE = 1 << 0;

        /**
         * `XIAN_WEB_ENGINE_VIEW_FLAG_INPUT_SINGLE_PRODUCER`：宿主保证只有一个线程发送输入（更快 push 路径）。
         * 违反该约定属于未定义行为。
         */
        public static final int INPUT_SINGLE_PRODUCER = 1 << 1;

        /**
         * `XIAN_WEB_ENGINE_VIEW_FLAG_UNSAFE_NO_PRODUCER_FENCE`：不提供 producer fence（更快但不安全）。
         */
        public static final int UNSAFE_NO_PRODUCER_FENCE = 1 << 2;

        private ViewFlag() {
        }
    }

    public static final class Native {
        public static final MethodHandle abi_version = LIBRARY.loadFunctionCritical("xian_web_engine_abi_version", FunctionDescriptor.of(Types.U32));

        public static final MethodHandle config_init = LIBRARY.loadFunctionCritical("xian_web_engine_config_init", FunctionDescriptor.ofVoid(Types.U64));
        public static final MethodHandle create = LIBRARY.loadFunctionCritical("xian_web_engine_create", FunctionDescriptor.of(Types.U64, Types.U64));
        public static final MethodHandle destroy = LIBRARY.loadFunctionCritical("xian_web_engine_destroy", FunctionDescriptor.ofVoid(Types.U64));
        public static final MethodHandle tick = LIBRARY.loadFunctionCritical("xian_web_engine_tick", FunctionDescriptor.ofVoid(Types.U64));

        public static final MethodHandle view_config_init = LIBRARY.loadFunctionCritical("xian_web_engine_view_config_init", FunctionDescriptor.ofVoid(Types.U64));
        public static final MethodHandle view_create = LIBRARY.loadFunctionCritical("xian_web_engine_view_create", FunctionDescriptor.of(Types.U64, Types.U64));
        public static final MethodHandle view_destroy = LIBRARY.loadFunctionCritical("xian_web_engine_view_destroy", FunctionDescriptor.ofVoid(Types.U64));
        public static final MethodHandle view_set_active = LIBRARY.loadFunctionCritical("xian_web_engine_view_set_active", FunctionDescriptor.ofVoid(Types.U64, Types.U8));
        public static final MethodHandle view_load_url = LIBRARY.loadFunctionCritical("xian_web_engine_view_load_url", FunctionDescriptor.of(Types.BOOL, Types.U64, Types.U64));
        public static final MethodHandle view_resize = LIBRARY.loadFunctionCritical("xian_web_engine_view_resize", FunctionDescriptor.ofVoid(Types.U64, Types.U32, Types.U32));
        public static final MethodHandle view_send_input_events = LIBRARY.loadFunctionCritical("xian_web_engine_view_send_input_events", FunctionDescriptor.of(Types.U32, Types.U64, Types.U64, Types.U32));
        public static final MethodHandle view_acquire_frame = LIBRARY.loadFunctionCritical("xian_web_engine_view_acquire_frame", FunctionDescriptor.of(Types.BOOL, Types.U64, Types.U64));
        public static final MethodHandle view_release_frame = LIBRARY.loadFunctionCritical("xian_web_engine_view_release_frame", FunctionDescriptor.ofVoid(Types.U64, Types.U32, Types.U64));

        public static final MethodHandle views_acquire_frames = LIBRARY.loadFunctionCritical("xian_web_engine_views_acquire_frames", FunctionDescriptor.of(Types.U32, Types.U64, Types.U64, Types.U64, Types.U32));
        public static final MethodHandle views_release_frames = LIBRARY.loadFunctionCritical("xian_web_engine_views_release_frames", FunctionDescriptor.ofVoid(Types.U64, Types.U64, Types.U64, Types.U32));

        /**
         * `XIAN_WEB_ENGINE_FLAG_NO_PARK`：Servo 线程 idle 时不 park（busy-spin）。
         * 低延迟但会显著增加空闲 CPU 占用。
         */
        public static final int NO_PARK = 1 << 0;

        /**
         * `XIAN_WEB_ENGINE_VIEW_FLAG_UNSAFE_NO_CONSUMER_FENCE`：忽略 consumer fence（更快但不安全）。
         */
        public static final int UNSAFE_NO_CONSUMER_FENCE = 1 << 0;

        /**
         * `XIAN_WEB_ENGINE_VIEW_FLAG_INPUT_SINGLE_PRODUCER`：宿主保证只有一个线程发送输入（更快 push 路径）。
         * 违反该约定属于未定义行为。
         */
        public static final int INPUT_SINGLE_PRODUCER = 1 << 1;

        /**
         * `XIAN_WEB_ENGINE_VIEW_FLAG_UNSAFE_NO_PRODUCER_FENCE`：不提供 producer fence（更快但不安全）。
         */
        public static final int UNSAFE_NO_PRODUCER_FENCE = 1 << 2;

        public static final MemoryLayout GLFW_API = MemoryLayout.structLayout(
                ValueLayout.ADDRESS.withName("glfw_get_proc_address"),
                ValueLayout.ADDRESS.withName("glfw_make_context_current"),
                ValueLayout.ADDRESS.withName("glfw_default_window_hints"),
                ValueLayout.ADDRESS.withName("glfw_window_hint"),
                ValueLayout.ADDRESS.withName("glfw_get_window_attrib"),
                ValueLayout.ADDRESS.withName("glfw_create_window"),
                ValueLayout.ADDRESS.withName("glfw_destroy_window")
        );
        public static final MemoryLayout CONFIG = MemoryLayout.structLayout(
                ValueLayout.ADDRESS.withName("glfw_shared_window"),
                GLFW_API.withName("glfw_api"),
                ValueLayout.ADDRESS.withName("resources_dir"),
                ValueLayout.ADDRESS.withName("config_dir"),
                ValueLayout.JAVA_INT.withName("default_width"),
                ValueLayout.JAVA_INT.withName("default_height"),
                ValueLayout.JAVA_INT.withName("thread_pool_cap"),
                ValueLayout.JAVA_INT.withName("engine_flags")
        );
        public static final MemoryLayout VIEW_CONFIG = MemoryLayout.structLayout(
                ValueLayout.ADDRESS.withName("engine"),
                ValueLayout.JAVA_INT.withName("width"),
                ValueLayout.JAVA_INT.withName("height"),
                ValueLayout.JAVA_INT.withName("target_fps"),
                ValueLayout.JAVA_INT.withName("view_flags")
        );
        public static final MemoryLayout FRAME = MemoryLayout.structLayout(
                ValueLayout.JAVA_LONG.withName("producer_fence"),
                ValueLayout.JAVA_INT.withName("texture_id"),
                ValueLayout.JAVA_INT.withName("slot"),
                ValueLayout.JAVA_INT.withName("width"),
                ValueLayout.JAVA_INT.withName("height")
        );
        public static final MemoryLayout INPUT_EVENT = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("kind"),
                ValueLayout.JAVA_FLOAT.withName("x"),
                ValueLayout.JAVA_FLOAT.withName("y"),
                ValueLayout.JAVA_INT.withName("modifiers"),

                ValueLayout.JAVA_INT.withName("mouse_button"),
                ValueLayout.JAVA_INT.withName("mouse_action"),

                ValueLayout.JAVA_DOUBLE.withName("wheel_delta_x"),
                ValueLayout.JAVA_DOUBLE.withName("wheel_delta_y"),
                ValueLayout.JAVA_DOUBLE.withName("wheel_delta_z"),
                ValueLayout.JAVA_INT.withName("wheel_mode"),

                ValueLayout.JAVA_INT.withName("key_state"),
                ValueLayout.JAVA_INT.withName("key_location"),
                ValueLayout.JAVA_INT.withName("repeat"),
                ValueLayout.JAVA_INT.withName("is_composing"),
                ValueLayout.JAVA_INT.withName("key_codepoint"),
                ValueLayout.JAVA_INT.withName("glfw_key")
        );

        private Native() {}
    }

    /**
     * 获取ABI版本
     * @return ABI版本
     */
    public static int abi_version() {
        try {
            return (int) Native.abi_version.invokeExact();
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 初始化引擎配置项
     */
    public static void config_init(long config_address) {
        try {
            Native.config_init.invokeExact(config_address);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 创建引擎实例
     * @return 引擎实例的内存地址
     */
    public static long create(long config_address) {
        try {
            return (long) Native.create.invokeExact(config_address);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 销毁引擎实例
     */
    public static void destroy(long engine_address) {
        try {
            Native.destroy.invokeExact(engine_address);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 触发引擎 Tick
     */
    public static void tick(long engine_address) {
        try {
            Native.tick.invokeExact(engine_address);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 初始化视图配置项
     */
    public static void view_config_init(long config_address) {
        try {
            Native.view_config_init.invokeExact(config_address);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 创建视图实例
     * @return 视图实例的内存地址
     */
    public static long view_create(long config_address) {
        try {
            return (long) Native.view_create.invokeExact(config_address);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 销毁视图实例
     */
    public static void view_destroy(long view_address) {
        try {
            Native.view_destroy.invokeExact(view_address);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 设置视图实例的活动情况
     */
    public static void view_set_active(long view_address, boolean is_active) {
        try {
            Native.view_set_active.invokeExact(view_address, is_active);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 设置视图实例加载的 URL 地址
     * @return URL 是否是标准 C 字符串
     */
    public static boolean view_load_url(long view_address, long url_address) {
        try {
            return (boolean) Native.view_load_url.invokeExact(view_address, url_address);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 调整视图实例的大小
     */
    public static void view_resize(long view_address, int width, int height) {
        try {
            Native.view_resize.invokeExact(view_address, width, height);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 发送事件到视图实例
     * @return 成功入队的事件个数
     */
    public static int view_send_input_events(long view_address, long input_event_address, int count) {
        try {
            return (int) Native.view_send_input_events.invokeExact(view_address, input_event_address, count);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 获取视图的最新 READY 帧
     * @return 是否成功获取
     */
    public static boolean view_acquire_frame(long views_address, long out_frame_address) {
        try {
            return (boolean) Native.view_acquire_frame.invokeExact(views_address, out_frame_address);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 释放视图的 READY 帧
     */
    public static void view_release_frame(long views_address, int slots, long consumer_fence) {
        try {
            Native.view_release_frame.invokeExact(views_address, slots, consumer_fence);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 批量获取多个视图的最新 READY 帧
     * @return 成功获取的帧个数
     */
    public static int views_acquire_frames(long views_address, long out_view_indices_address, long out_frames_address, int count) {
        try {
            return (int) Native.views_acquire_frames.invokeExact(views_address, out_view_indices_address, out_frames_address, count);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    /**
     * 批量释放多个视图的 READY 帧
     */
    public static void views_release_frames(long views_address, long slots_address, long consumer_fences_address, int count) {
        try {
            Native.views_release_frames.invokeExact(views_address, slots_address, consumer_fences_address, count);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }

    private Abi() {
    }
}
