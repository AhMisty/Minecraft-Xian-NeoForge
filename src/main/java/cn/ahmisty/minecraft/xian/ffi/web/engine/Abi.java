package cn.ahmisty.minecraft.xian.ffi.web.engine;

import cn.ahmisty.minecraft.xian.ffi.Library;
import org.lwjgl.glfw.GLFW;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class Abi {
    public static final Arena ARENA = Arena.global();
    private static final Library LIBRARY;
    static {
        try {
            LIBRARY = new Library("xian_web_engine", ARENA);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public static final MethodHandle abi_version = LIBRARY.loadFunctionCritical("xian_web_engine_abi_version", FunctionDescriptor.of(ValueLayout.JAVA_INT));

    public static final MethodHandle config_init = LIBRARY.loadFunctionCritical("xian_web_engine_config_init", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    public static final MethodHandle create = LIBRARY.loadFunctionCritical("xian_web_engine_create", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static final MethodHandle destroy = LIBRARY.loadFunctionCritical("xian_web_engine_destroy", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    public static final MethodHandle tick = LIBRARY.loadFunctionCritical("xian_web_engine_tick", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    public static final MethodHandle view_config_init = LIBRARY.loadFunctionCritical("xian_web_engine_view_config_init", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    public static final MethodHandle view_create = LIBRARY.loadFunctionCritical("xian_web_engine_view_create", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static final MethodHandle view_destroy = LIBRARY.loadFunctionCritical("xian_web_engine_view_destroy", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    public static final MethodHandle view_set_active = LIBRARY.loadFunctionCritical("xian_web_engine_view_set_active", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE));
    public static final MethodHandle view_load_url = LIBRARY.loadFunctionCritical("xian_web_engine_view_load_url", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static final MethodHandle view_resize = LIBRARY.loadFunctionCritical("xian_web_engine_view_resize", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    public static final MethodHandle view_send_input_events = LIBRARY.loadFunctionCritical("xian_web_engine_view_send_input_events", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static final MethodHandle view_acquire_frame = LIBRARY.loadFunctionCritical("xian_web_engine_view_acquire_frame", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static final MethodHandle view_release_frame = LIBRARY.loadFunctionCritical("xian_web_engine_view_release_frame", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));

    public static final MethodHandle views_acquire_frames = LIBRARY.loadFunctionCritical("xian_web_engine_views_acquire_frames", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    public static final MethodHandle views_release_frames = LIBRARY.loadFunctionCritical("xian_web_engine_views_release_frames", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    public static final int FLAG_NO_PARK = 1;
    public static final int VIEW_FLAG_UNSAFE_NO_CONSUMER_FENCE = 1;
    public static final int VIEW_FLAG_INPUT_SINGLE_PRODUCER = 1 << 1;
    public static final int VIEW_FLAG_UNSAFE_NO_PRODUCER_FENCE = 1 << 2;

    public static final MemoryLayout EMBEDDER_GLFW_API = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("glfw_get_proc_address"),
            ValueLayout.ADDRESS.withName("glfw_make_context_current"),
            ValueLayout.ADDRESS.withName("glfw_default_window_hints"),
            ValueLayout.ADDRESS.withName("glfw_window_hint"),
            ValueLayout.ADDRESS.withName("glfw_get_window_attrib"),
            ValueLayout.ADDRESS.withName("glfw_create_window"),
            ValueLayout.ADDRESS.withName("glfw_destroy_window")
    );
    public static final long OFFSET_EMBEDDER_GLFW_API_GET_PROC_ADDRESS =
            EMBEDDER_GLFW_API.byteOffset(MemoryLayout.PathElement.groupElement("glfw_get_proc_address"));
    public static final long OFFSET_EMBEDDER_GLFW_API_MAKE_CONTEXT_CURRENT =
            EMBEDDER_GLFW_API.byteOffset(MemoryLayout.PathElement.groupElement("glfw_make_context_current"));
    public static final long OFFSET_EMBEDDER_GLFW_API_DEFAULT_WINDOW_HINTS =
            EMBEDDER_GLFW_API.byteOffset(MemoryLayout.PathElement.groupElement("glfw_default_window_hints"));
    public static final long OFFSET_EMBEDDER_GLFW_API_WINDOW_HINT =
            EMBEDDER_GLFW_API.byteOffset(MemoryLayout.PathElement.groupElement("glfw_window_hint"));
    public static final long OFFSET_EMBEDDER_GLFW_API_GET_WINDOW_ATTRIB =
            EMBEDDER_GLFW_API.byteOffset(MemoryLayout.PathElement.groupElement("glfw_get_window_attrib"));
    public static final long OFFSET_EMBEDDER_GLFW_API_CREATE_WINDOW =
            EMBEDDER_GLFW_API.byteOffset(MemoryLayout.PathElement.groupElement("glfw_create_window"));
    public static final long OFFSET_EMBEDDER_GLFW_API_DESTROY_WINDOW =
            EMBEDDER_GLFW_API.byteOffset(MemoryLayout.PathElement.groupElement("glfw_destroy_window"));

    public static final MemorySegment DEFAULT_EMBEDDER_GLFW_API;
    static {
        MemorySegment api = ARENA.allocate(EMBEDDER_GLFW_API);
        api.set(ValueLayout.ADDRESS, OFFSET_EMBEDDER_GLFW_API_GET_PROC_ADDRESS, MemorySegment.ofAddress(GLFW.Functions.GetProcAddress));
        api.set(ValueLayout.ADDRESS, OFFSET_EMBEDDER_GLFW_API_MAKE_CONTEXT_CURRENT, MemorySegment.ofAddress(GLFW.Functions.MakeContextCurrent));
        api.set(ValueLayout.ADDRESS, OFFSET_EMBEDDER_GLFW_API_DEFAULT_WINDOW_HINTS, MemorySegment.ofAddress(GLFW.Functions.DefaultWindowHints));
        api.set(ValueLayout.ADDRESS, OFFSET_EMBEDDER_GLFW_API_WINDOW_HINT, MemorySegment.ofAddress(GLFW.Functions.WindowHint));
        api.set(ValueLayout.ADDRESS, OFFSET_EMBEDDER_GLFW_API_GET_WINDOW_ATTRIB, MemorySegment.ofAddress(GLFW.Functions.GetWindowAttrib));
        api.set(ValueLayout.ADDRESS, OFFSET_EMBEDDER_GLFW_API_CREATE_WINDOW, MemorySegment.ofAddress(GLFW.Functions.CreateWindow));
        api.set(ValueLayout.ADDRESS, OFFSET_EMBEDDER_GLFW_API_DESTROY_WINDOW, MemorySegment.ofAddress(GLFW.Functions.DestroyWindow));
        DEFAULT_EMBEDDER_GLFW_API = api;
    }

    public static final MemoryLayout CONFIG = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("glfw_shared_window"),
            EMBEDDER_GLFW_API.withName("glfw_api"),
            ValueLayout.ADDRESS.withName("resources_dir"),
            ValueLayout.ADDRESS.withName("config_dir"),
            ValueLayout.JAVA_INT.withName("default_width"),
            ValueLayout.JAVA_INT.withName("default_height"),
            ValueLayout.JAVA_INT.withName("thread_pool_cap"),
            ValueLayout.JAVA_INT.withName("engine_flags")
    );
    public static final long OFFSET_CONFIG_GLFW_SHARED_WINDOW =
            CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("glfw_shared_window"));
    public static final long OFFSET_CONFIG_GLFW_API =
            CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("glfw_api"));
    public static final long OFFSET_CONFIG_RESOURCES_DIR =
            CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("resources_dir"));
    public static final long OFFSET_CONFIG_CONFIG_DIR =
            CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("config_dir"));
    public static final long OFFSET_CONFIG_DEFAULT_WIDTH =
            CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("default_width"));
    public static final long OFFSET_CONFIG_DEFAULT_HEIGHT =
            CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("default_height"));
    public static final long OFFSET_CONFIG_THREAD_POOL_CAP =
            CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("thread_pool_cap"));
    public static final long OFFSET_CONFIG_ENGINE_FLAGS =
            CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("engine_flags"));

    public static final MemoryLayout VIEW_CONFIG = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("engine"),
            ValueLayout.JAVA_INT.withName("width"),
            ValueLayout.JAVA_INT.withName("height"),
            ValueLayout.JAVA_INT.withName("target_fps"),
            ValueLayout.JAVA_INT.withName("view_flags")
    );
    public static final long OFFSET_VIEW_CONFIG_ENGINE =
            VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("engine"));
    public static final long OFFSET_VIEW_CONFIG_WIDTH =
            VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("width"));
    public static final long OFFSET_VIEW_CONFIG_HEIGHT=
            VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("height"));
    public static final long OFFSET_VIEW_CONFIG_TARGET_FPS =
            VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("target_fps"));
    public static final long OFFSET_VIEW_CONFIG_VIEW_FLAGS =
            VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("view_flags"));

    public static final MemoryLayout FRAME = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("producer_fence"),
            ValueLayout.JAVA_INT.withName("texture_id"),
            ValueLayout.JAVA_INT.withName("slot"),
            ValueLayout.JAVA_INT.withName("width"),
            ValueLayout.JAVA_INT.withName("height")
    );
    public static final long OFFSET_FRAME_PRODUCER_FENCE =
            FRAME.byteOffset(MemoryLayout.PathElement.groupElement("producer_fence"));
    public static final long OFFSET_FRAME_TEXTURE_ID =
            FRAME.byteOffset(MemoryLayout.PathElement.groupElement("texture_id"));
    public static final long OFFSET_FRAME_SLOT =
            FRAME.byteOffset(MemoryLayout.PathElement.groupElement("slot"));
    public static final long OFFSET_FRAME_WIDTH =
            FRAME.byteOffset(MemoryLayout.PathElement.groupElement("width"));
    public static final long OFFSET_FRAME_HEIGHT =
            FRAME.byteOffset(MemoryLayout.PathElement.groupElement("height"));

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
            ValueLayout.JAVA_INT.withName("glfw_key"),
            MemoryLayout.paddingLayout(4)
    );
    public static final long OFFSET_INPUT_EVENT_KIND =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("kind"));
    public static final long OFFSET_INPUT_EVENT_X =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("x"));
    public static final long OFFSET_INPUT_EVENT_Y =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("y"));
    public static final long OFFSET_INPUT_EVENT_MODIFIERS =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("modifiers"));
    public static final long OFFSET_INPUT_EVENT_MOUSE_BUTTON =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("mouse_button"));
    public static final long OFFSET_INPUT_EVENT_MOUSE_ACTION =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("mouse_action"));
    public static final long OFFSET_INPUT_EVENT_WHEEL_DELTA_X =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("wheel_delta_x"));
    public static final long OFFSET_INPUT_EVENT_WHEEL_DELTA_Y =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("wheel_delta_y"));
    public static final long OFFSET_INPUT_EVENT_WHEEL_DELTA_Z =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("wheel_delta_z"));
    public static final long OFFSET_INPUT_EVENT_WHEEL_MODE =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("wheel_mode"));
    public static final long OFFSET_INPUT_EVENT_KEY_STATE =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("key_state"));
    public static final long OFFSET_INPUT_EVENT_KEY_LOCATION =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("key_location"));
    public static final long OFFSET_INPUT_EVENT_REPEAT =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("repeat"));
    public static final long OFFSET_INPUT_EVENT_IS_COMPOSING =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("is_composing"));
    public static final long OFFSET_INPUT_EVENT_KEY_CODEPOINT =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("key_codepoint"));
    public static final long OFFSET_INPUT_EVENT_GLFW_KEY =
            INPUT_EVENT.byteOffset(MemoryLayout.PathElement.groupElement("glfw_key"));

    private Abi() {
    }
}
