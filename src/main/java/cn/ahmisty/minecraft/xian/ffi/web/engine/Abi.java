package cn.ahmisty.minecraft.xian.ffi.web.engine;

import cn.ahmisty.minecraft.xian.ffi.Library;
import cn.ahmisty.minecraft.xian.ffi.Types;
import org.lwjgl.glfw.GLFW;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public final class Abi {
    public static final String LIB_NAME = "xian_web_engine";
    public static final Arena ARENA = Arena.ofShared();
    private static final Library LIBRARY = new Library(LIB_NAME, ARENA);

    private static final FunctionDescriptor ABI_VERSION = FunctionDescriptor.of(Types.U32);

    private static final FunctionDescriptor CONFIG_INIT = FunctionDescriptor.ofVoid(Types.U64);
    private static final FunctionDescriptor CREATE = FunctionDescriptor.of(Types.U64, Types.U64);
    private static final FunctionDescriptor DESTROY = FunctionDescriptor.ofVoid(Types.U64);
    private static final FunctionDescriptor TICK = FunctionDescriptor.ofVoid(Types.U64);

    private static final FunctionDescriptor VIEW_CONFIG_INIT = FunctionDescriptor.ofVoid(Types.U64);
    private static final FunctionDescriptor VIEW_CREATE = FunctionDescriptor.of(Types.U64, Types.U64);
    private static final FunctionDescriptor VIEW_DESTROY = FunctionDescriptor.ofVoid(Types.U64);
    private static final FunctionDescriptor VIEW_SET_ACTIVE = FunctionDescriptor.ofVoid(Types.U64, Types.U8);
    private static final FunctionDescriptor VIEW_LOAD_URL = FunctionDescriptor.of(Types.BOOL, Types.U64, Types.U64);
    private static final FunctionDescriptor VIEW_RESIZE = FunctionDescriptor.ofVoid(Types.U64, Types.U32, Types.U32);
    private static final FunctionDescriptor VIEW_SEND_INPUT_EVENTS = FunctionDescriptor.of(Types.U32, Types.U64, Types.U64, Types.U32);

    private static final FunctionDescriptor VIEWS_ACQUIRE_FRAMES = FunctionDescriptor.of(Types.U32, Types.U64, Types.U64, Types.U64, Types.U32);
    private static final FunctionDescriptor VIEWS_RELEASE_FRAMES = FunctionDescriptor.ofVoid(Types.U64, Types.U64, Types.U64, Types.U32);

    public static final MemoryLayout GLFW_API = MemoryLayout.structLayout(
            Types.U64.withName("glfw_get_proc_address"),
            Types.U64.withName("glfw_make_context_current"),
            Types.U64.withName("glfw_default_window_hints"),
            Types.U64.withName("glfw_window_hint"),
            Types.U64.withName("glfw_get_window_attrib"),
            Types.U64.withName("glfw_create_window"),
            Types.U64.withName("glfw_destroy_window")
    ).withName("GlfwApi");

    public static final MemoryLayout CONFIG = MemoryLayout.structLayout(
            Types.U32.withName("struct_size"),
            Types.U32.withName("abi_version"),
            Types.U64.withName("glfw_shared_window"),
            GLFW_API.withName("glfw_api"),
            Types.U32.withName("default_width"),
            Types.U32.withName("default_height"),
            Types.U32.withName("thread_pool_cap"),
            Types.U32.withName("engine_flags"),
            Types.U64.withName("resources_dir"),
            Types.U64.withName("config_dir")
    ).withName("Config");

    public static final MemoryLayout VIEW_CONFIG = MemoryLayout.structLayout(
            Types.U32.withName("struct_size"),
            Types.U32.withName("abi_version"),
            Types.U64.withName("engine"),
            Types.U32.withName("width"),
            Types.U32.withName("height"),
            Types.U32.withName("target_fps"),
            Types.U32.withName("view_flags")
    ).withName("ViewConfig");

    public static final MemoryLayout FRAME = MemoryLayout.structLayout(
            Types.U32.withName("slot"),
            Types.U32.withName("texture_id"),
            Types.U64.withName("producer_fence"),
            Types.U32.withName("width"),
            Types.U32.withName("height")
    ).withName("Frame");

    public static final MemoryLayout INPUT_EVENT = MemoryLayout.structLayout(
            Types.U32.withName("kind"),
            Types.F32.withName("x"),
            Types.F32.withName("y"),
            Types.U32.withName("modifiers"),
            Types.U32.withName("mouse_button"),
            Types.U32.withName("mouse_action"),
            Types.F64.withName("wheel_delta_x"),
            Types.F64.withName("wheel_delta_y"),
            Types.F64.withName("wheel_delta_z"),
            Types.U32.withName("wheel_mode"),
            Types.U32.withName("key_state"),
            Types.U32.withName("key_location"),
            Types.U32.withName("repeat"),
            Types.U32.withName("is_composing"),
            Types.U32.withName("key_codepoint"),
            Types.U32.withName("glfw_key"),
            MemoryLayout.paddingLayout(4)
    ).withName("InputEvent");

    public static final MethodHandle abi_version = LIBRARY.loadFunctionCritical("xian_web_engine_abi_version", ABI_VERSION);

    public static final MethodHandle config_init = LIBRARY.loadFunctionCritical("xian_web_engine_config_init", CONFIG_INIT);
    public static final MethodHandle create = LIBRARY.loadFunctionCritical("xian_web_engine_create", CREATE);
    public static final MethodHandle destroy = LIBRARY.loadFunctionCritical("xian_web_engine_destroy", DESTROY);
    public static final MethodHandle tick = LIBRARY.loadFunctionCritical("xian_web_engine_tick", TICK);

    public static final MethodHandle view_config_init = LIBRARY.loadFunctionCritical("xian_web_engine_view_config_init", VIEW_CONFIG_INIT);
    public static final MethodHandle view_create = LIBRARY.loadFunctionCritical("xian_web_engine_view_create", VIEW_CREATE);
    public static final MethodHandle view_destroy = LIBRARY.loadFunctionCritical("xian_web_engine_view_destroy", VIEW_DESTROY);
    public static final MethodHandle view_set_active = LIBRARY.loadFunctionCritical("xian_web_engine_view_set_active", VIEW_SET_ACTIVE);
    public static final MethodHandle view_load_url = LIBRARY.loadFunctionCritical("xian_web_engine_view_load_url", VIEW_LOAD_URL);
    public static final MethodHandle view_resize = LIBRARY.loadFunctionCritical("xian_web_engine_view_resize", VIEW_RESIZE);
    public static final MethodHandle view_send_input_events = LIBRARY.loadFunctionCritical("xian_web_engine_view_send_input_events", VIEW_SEND_INPUT_EVENTS);

    public static final MethodHandle views_acquire_frames = LIBRARY.loadFunctionCritical("xian_web_engine_views_acquire_frames", VIEWS_ACQUIRE_FRAMES);
    public static final MethodHandle views_release_frames = LIBRARY.loadFunctionCritical("xian_web_engine_views_release_frames", VIEWS_RELEASE_FRAMES);

    public static final MemorySegment DEFAULT_GLFW_API = Abi.ARENA.allocate(GLFW_API);

    static {
        try {
            GLFW_API.varHandle(MemoryLayout.PathElement.groupElement("glfw_get_proc_address")).set(DEFAULT_GLFW_API, 0L, GLFW.Functions.GetProcAddress);
            GLFW_API.varHandle(MemoryLayout.PathElement.groupElement("glfw_make_context_current")).set(DEFAULT_GLFW_API, 0L, GLFW.Functions.MakeContextCurrent);
            GLFW_API.varHandle(MemoryLayout.PathElement.groupElement("glfw_default_window_hints")).set(DEFAULT_GLFW_API, 0L, GLFW.Functions.DefaultWindowHints);
            GLFW_API.varHandle(MemoryLayout.PathElement.groupElement("glfw_window_hint")).set(DEFAULT_GLFW_API, 0L, GLFW.Functions.WindowHint);
            GLFW_API.varHandle(MemoryLayout.PathElement.groupElement("glfw_get_window_attrib")).set(DEFAULT_GLFW_API, 0L, GLFW.Functions.GetWindowAttrib);
            GLFW_API.varHandle(MemoryLayout.PathElement.groupElement("glfw_create_window")).set(DEFAULT_GLFW_API, 0L, GLFW.Functions.CreateWindow);
            GLFW_API.varHandle(MemoryLayout.PathElement.groupElement("glfw_destroy_window")).set(DEFAULT_GLFW_API, 0L, GLFW.Functions.DestroyWindow);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private Abi() {
    }
}
