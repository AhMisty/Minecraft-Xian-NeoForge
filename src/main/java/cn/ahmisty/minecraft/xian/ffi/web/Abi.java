package cn.ahmisty.minecraft.xian.ffi.web;

import cn.ahmisty.minecraft.xian.ffi.Library;

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

    public static final int XIAN_WEB_ENGINE_ABI_VERSION = 1;

    public static final int XIAN_WEB_ENGINE_GL_API_GL = 1;
    public static final int XIAN_WEB_ENGINE_GL_API_GLES = 2;

    public static final int XIAN_WEB_ENGINE_INPUT_KIND_MOUSE_MOVE = 1;
    public static final int XIAN_WEB_ENGINE_INPUT_KIND_MOUSE_BUTTON = 2;
    public static final int XIAN_WEB_ENGINE_INPUT_KIND_WHEEL = 3;
    public static final int XIAN_WEB_ENGINE_INPUT_KIND_KEY = 4;

    public static final int XIAN_WEB_ENGINE_MOD_SHIFT = 1;
    public static final int XIAN_WEB_ENGINE_MOD_CONTROL = 1 << 1;
    public static final int XIAN_WEB_ENGINE_MOD_ALT = 1 << 2;
    public static final int XIAN_WEB_ENGINE_MOD_META = 1 << 3;

    // View load status mapping (matches the native ABI).
    public static final int XIAN_WEB_ENGINE_LOAD_STATUS_STARTED = 0;
    public static final int XIAN_WEB_ENGINE_LOAD_STATUS_HEAD_PARSED = 1;
    public static final int XIAN_WEB_ENGINE_LOAD_STATUS_COMPLETE = 2;
    public static final int XIAN_WEB_ENGINE_LOAD_STATUS_INVALID = -1; // 0xFFFFFFFF

    public static final MemoryLayout GLFW_API = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("glfw_get_proc_address")
    );
    public static final long OFFSET_GLFW_API_GET_PROC_ADDRESS =
            GLFW_API.byteOffset(MemoryLayout.PathElement.groupElement("glfw_get_proc_address"));

    public static final MemoryLayout VIEW_CONFIG = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("width"),
            ValueLayout.JAVA_INT.withName("height"),
            ValueLayout.JAVA_FLOAT.withName("hidpi_scale_factor"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withName("initial_url")
    );
    public static final long OFFSET_VIEW_CONFIG_WIDTH =
            VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("width"));
    public static final long OFFSET_VIEW_CONFIG_HEIGHT=
            VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("height"));
    public static final long OFFSET_VIEW_CONFIG_HIDPI_SCALE_FACTOR =
            VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("hidpi_scale_factor"));
    public static final long OFFSET_VIEW_CONFIG_INITIAL_URL =
            VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("initial_url"));
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

    public static final MethodHandle abi_version =
            LIBRARY.loadFunctionCritical("xian_web_engine_abi_version", FunctionDescriptor.of(ValueLayout.JAVA_INT));

    public static final MethodHandle set_resources_dir =
            LIBRARY.loadFunctionCritical("xian_web_engine_set_resources_dir", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS));
    public static final MethodHandle set_config_dir =
            LIBRARY.loadFunctionCritical("xian_web_engine_set_config_dir", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS));
    public static final MethodHandle set_thread_pool_cap =
            LIBRARY.loadFunctionCritical("xian_web_engine_set_thread_pool_cap", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT));

    public static final MethodHandle set_glfw_api =
            LIBRARY.loadFunctionCritical("xian_web_engine_set_glfw_api", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, GLFW_API));
    public static final MethodHandle set_gl_api =
            LIBRARY.loadFunctionCritical("xian_web_engine_set_gl_api", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT));

    public static final MethodHandle init =
            LIBRARY.loadFunctionCritical("xian_web_engine_init", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN));
    public static final MethodHandle tick =
            LIBRARY.loadFunctionCritical("xian_web_engine_tick", FunctionDescriptor.of(ValueLayout.JAVA_INT));

    public static final MethodHandle view_config_init =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_config_init", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    public static final MethodHandle view_create =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_create", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static final MethodHandle view_destroy =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_destroy", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

    public static final MethodHandle view_load_url =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_load_url", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    public static final MethodHandle view_resize =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_resize", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    public static final MethodHandle view_set_hidpi_scale_factor =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_set_hidpi_scale_factor", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.JAVA_FLOAT));
    public static final MethodHandle view_texture_id =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_texture_id", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    public static final MethodHandle view_paint =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_paint", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS));
    public static final MethodHandle view_load_status =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_load_status", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    public static final MethodHandle view_copy_url_utf8 =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_copy_url_utf8", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    public static final MethodHandle view_send_input_events =
            LIBRARY.loadFunctionCritical("xian_web_engine_view_send_input_events", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    private Abi() {
    }
}
