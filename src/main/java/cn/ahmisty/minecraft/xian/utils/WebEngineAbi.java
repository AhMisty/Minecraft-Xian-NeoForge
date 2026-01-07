package cn.ahmisty.minecraft.xian.utils;

import cn.ahmisty.minecraft.xian.ffi.Library;
import cn.ahmisty.minecraft.xian.ffi.Types;
import cn.ahmisty.minecraft.xian.ffi.Util;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

public final class WebEngineAbi {
    public static final String LIB_NAME = "web_engine";

    public static final int INPUT_KIND_MOUSE_MOVE = 1;
    public static final int INPUT_KIND_MOUSE_BUTTON = 2;
    public static final int INPUT_KIND_WHEEL = 3;
    public static final int INPUT_KIND_KEY = 4;

    public static final int FLAG_NO_PARK = 1 << 0;

    public static final int VIEW_FLAG_UNSAFE_NO_CONSUMER_FENCE = 1 << 0;
    public static final int VIEW_FLAG_INPUT_SINGLE_PRODUCER = 1 << 1;
    public static final int VIEW_FLAG_UNSAFE_NO_PRODUCER_FENCE = 1 << 2;

    public static final ValueLayout.OfInt U32 = Types.U32;
    public static final ValueLayout.OfLong U64 = Types.U64;
    public static final ValueLayout.OfFloat F32 = Types.F32;
    public static final ValueLayout.OfDouble F64 = Types.F64;

    public static final MemoryLayout EMBEDDER_GLFW_API = MemoryLayout.structLayout(
            U64.withName("glfw_get_proc_address"),
            U64.withName("glfw_make_context_current"),
            U64.withName("glfw_default_window_hints"),
            U64.withName("glfw_window_hint"),
            U64.withName("glfw_get_window_attrib"),
            U64.withName("glfw_create_window"),
            U64.withName("glfw_destroy_window")
    ).withName("EmbedderGlfwApi");

    public static final MemoryLayout ENGINE_CONFIG = MemoryLayout.structLayout(
            U32.withName("struct_size"),
            U32.withName("abi_version"),
            U64.withName("glfw_shared_window"),
            EMBEDDER_GLFW_API.withName("glfw_api"),
            U32.withName("default_width"),
            U32.withName("default_height"),
            U32.withName("thread_pool_cap"),
            U32.withName("engine_flags"),
            U64.withName("resources_dir"),
            U64.withName("config_dir")
    ).withName("WebEngineConfig");

    public static final MemoryLayout VIEW_CONFIG = MemoryLayout.structLayout(
            U32.withName("struct_size"),
            U32.withName("abi_version"),
            U64.withName("engine"),
            U32.withName("width"),
            U32.withName("height"),
            U32.withName("target_fps"),
            U32.withName("view_flags")
    ).withName("WebEngineViewConfig");

    public static final MemoryLayout FRAME = MemoryLayout.structLayout(
            U32.withName("slot"),
            U32.withName("texture_id"),
            U64.withName("producer_fence"),
            U32.withName("width"),
            U32.withName("height")
    ).withName("WebEngineFrame");

    public static final MemoryLayout INPUT_EVENT = MemoryLayout.structLayout(
            U32.withName("kind"),
            F32.withName("x"),
            F32.withName("y"),
            U32.withName("modifiers"),
            U32.withName("mouse_button"),
            U32.withName("mouse_action"),
            F64.withName("wheel_delta_x"),
            F64.withName("wheel_delta_y"),
            F64.withName("wheel_delta_z"),
            U32.withName("wheel_mode"),
            U32.withName("key_state"),
            U32.withName("key_location"),
            U32.withName("repeat"),
            U32.withName("is_composing"),
            U32.withName("key_codepoint"),
            U32.withName("glfw_key"),
            MemoryLayout.paddingLayout(4)
    ).withName("WebEngineInputEvent");

    static {
        assertLayout("EMBEDDER_GLFW_API", EMBEDDER_GLFW_API, 56, 8);
        assertLayout("ENGINE_CONFIG", ENGINE_CONFIG, 104, 8);
        assertLayout("VIEW_CONFIG", VIEW_CONFIG, 32, 8);
        assertLayout("FRAME", FRAME, 24, 8);
        assertLayout("INPUT_EVENT", INPUT_EVENT, 80, 8);
    }

    private static final FunctionDescriptor ABI_VERSION = FunctionDescriptor.of(U32);

    private static final FunctionDescriptor ENGINE_CONFIG_INIT = FunctionDescriptor.ofVoid(U64);
    private static final FunctionDescriptor ENGINE_CREATE = FunctionDescriptor.of(U64, U64);
    private static final FunctionDescriptor ENGINE_DESTROY = FunctionDescriptor.ofVoid(U64);
    private static final FunctionDescriptor ENGINE_TICK = FunctionDescriptor.ofVoid(U64);

    private static final FunctionDescriptor VIEW_CONFIG_INIT = FunctionDescriptor.ofVoid(U64);
    private static final FunctionDescriptor VIEW_CREATE = FunctionDescriptor.of(U64, U64);
    private static final FunctionDescriptor VIEW_DESTROY = FunctionDescriptor.ofVoid(U64);
    private static final FunctionDescriptor VIEW_SET_ACTIVE = FunctionDescriptor.ofVoid(U64, Types.U8);
    private static final FunctionDescriptor VIEW_LOAD_URL = FunctionDescriptor.of(Types.BOOL, U64, U64);
    private static final FunctionDescriptor VIEW_RESIZE = FunctionDescriptor.ofVoid(U64, U32, U32);
    private static final FunctionDescriptor VIEW_SEND_INPUT_EVENTS = FunctionDescriptor.of(U32, U64, U64, U32);

    private static final FunctionDescriptor VIEWS_ACQUIRE_FRAMES = FunctionDescriptor.of(U32, U64, U64, U64, U32);
    private static final FunctionDescriptor VIEWS_RELEASE_FRAMES = FunctionDescriptor.ofVoid(U64, U64, U64, U32);

    private final Library library;

    private final MethodHandle web_engine_abi_version;

    private final MethodHandle web_engine_config_init;
    private final MethodHandle web_engine_create;
    private final MethodHandle web_engine_destroy;
    private final MethodHandle web_engine_tick;

    private final MethodHandle web_engine_view_config_init;
    private final MethodHandle web_engine_view_create;
    private final MethodHandle web_engine_view_destroy;
    private final MethodHandle web_engine_view_set_active;
    private final MethodHandle web_engine_view_load_url;
    private final MethodHandle web_engine_view_resize;
    private final MethodHandle web_engine_view_send_input_events;

    private final MethodHandle web_engine_views_acquire_frames;
    private final MethodHandle web_engine_views_release_frames;

    public WebEngineAbi(Library library) {
        this.library = Objects.requireNonNull(library, "library");

        this.web_engine_abi_version = Util.require(library.loadFunctionCritical("xian_web_engine_abi_version", ABI_VERSION), "xian_web_engine_abi_version");

        this.web_engine_config_init = Util.require(library.loadFunctionCritical("xian_web_engine_config_init", ENGINE_CONFIG_INIT), "xian_web_engine_config_init");
        this.web_engine_create = Util.require(library.loadFunctionCritical("xian_web_engine_create", ENGINE_CREATE), "xian_web_engine_create");
        this.web_engine_destroy = Util.require(library.loadFunctionCritical("xian_web_engine_destroy", ENGINE_DESTROY), "xian_web_engine_destroy");
        this.web_engine_tick = Util.require(library.loadFunctionCritical("xian_web_engine_tick", ENGINE_TICK), "xian_web_engine_tick");

        this.web_engine_view_config_init = Util.require(library.loadFunctionCritical("xian_web_engine_view_config_init", VIEW_CONFIG_INIT), "xian_web_engine_view_config_init");
        this.web_engine_view_create = Util.require(library.loadFunctionCritical("xian_web_engine_view_create", VIEW_CREATE), "xian_web_engine_view_create");
        this.web_engine_view_destroy = Util.require(library.loadFunctionCritical("xian_web_engine_view_destroy", VIEW_DESTROY), "xian_web_engine_view_destroy");
        this.web_engine_view_set_active = Util.require(library.loadFunctionCritical("xian_web_engine_view_set_active", VIEW_SET_ACTIVE), "xian_web_engine_view_set_active");
        this.web_engine_view_load_url = Util.require(library.loadFunctionCritical("xian_web_engine_view_load_url", VIEW_LOAD_URL), "xian_web_engine_view_load_url");
        this.web_engine_view_resize = Util.require(library.loadFunctionCritical("xian_web_engine_view_resize", VIEW_RESIZE), "xian_web_engine_view_resize");
        this.web_engine_view_send_input_events = Util.require(library.loadFunctionCritical("xian_web_engine_view_send_input_events", VIEW_SEND_INPUT_EVENTS), "xian_web_engine_view_send_input_events");

        this.web_engine_views_acquire_frames = Util.require(library.loadFunctionCritical("xian_web_engine_views_acquire_frames", VIEWS_ACQUIRE_FRAMES), "xian_web_engine_views_acquire_frames");
        this.web_engine_views_release_frames = Util.require(library.loadFunctionCritical("xian_web_engine_views_release_frames", VIEWS_RELEASE_FRAMES), "xian_web_engine_views_release_frames");
    }

    public Library library() {
        return library;
    }

    public int abiVersion() {
        try {
            return (int) web_engine_abi_version.invokeExact();
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public void engineConfigInit(MemorySegment config) {
        try {
            web_engine_config_init.invokeExact(Util.address(config));
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public long engineCreate(MemorySegment config) {
        try {
            return (long) web_engine_create.invokeExact(Util.address(config));
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public void engineDestroy(long engine) {
        try {
            web_engine_destroy.invokeExact(engine);
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public void engineTick(long engine) {
        try {
            web_engine_tick.invokeExact(engine);
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public void viewConfigInit(MemorySegment config) {
        try {
            web_engine_view_config_init.invokeExact(Util.address(config));
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public long viewCreate(MemorySegment config) {
        try {
            return (long) web_engine_view_create.invokeExact(Util.address(config));
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public void viewDestroy(long view) {
        try {
            web_engine_view_destroy.invokeExact(view);
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public void viewSetActive(long view, boolean active) {
        try {
            web_engine_view_set_active.invokeExact(view, (byte) (active ? 1 : 0));
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public boolean viewLoadUrl(long view, long url) {
        try {
            return (boolean) web_engine_view_load_url.invokeExact(view, url);
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public void viewResize(long view, int width, int height) {
        try {
            web_engine_view_resize.invokeExact(view, width, height);
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public int viewSendInputEvents(long view, long events, int count) {
        try {
            return (int) web_engine_view_send_input_events.invokeExact(view, events, count);
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public int viewsAcquireFrames(long views, long outViewIndices, long outFrames, int count) {
        try {
            return (int) web_engine_views_acquire_frames.invokeExact(views, outViewIndices, outFrames, count);
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public void viewsReleaseFrames(long views, long slots, long consumerFences, int count) {
        try {
            web_engine_views_release_frames.invokeExact(views, slots, consumerFences, count);
        } catch (Throwable t) {
            throw Util.rethrow(t);
        }
    }

    public static WebEngineAbi load(Arena arena) {
        Objects.requireNonNull(arena, "arena");
        return new WebEngineAbi(new Library(LIB_NAME, arena));
    }

    public static WebEngineAbi shared() {
        return Shared.INSTANCE;
    }

    private static final class Shared {
        private static final Arena ARENA = Arena.ofShared();
        private static final WebEngineAbi INSTANCE = WebEngineAbi.load(ARENA);
    }

    private static void assertLayout(String name, MemoryLayout layout, long expectedSize, long expectedAlignment) {
        long size = layout.byteSize();
        long alignment = layout.byteAlignment();
        if (size != expectedSize || alignment != expectedAlignment) {
            throw new IllegalStateException(
                    name + " layout mismatch: size=" + size + ", align=" + alignment +
                            " (expected size=" + expectedSize + ", align=" + expectedAlignment + ")"
            );
        }
    }

    public static final class EngineConfig {
        public static final MemoryLayout LAYOUT = ENGINE_CONFIG;
        public static final long SIZE = LAYOUT.byteSize();

        public static final long GLFW_SHARED_WINDOW = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("glfw_shared_window"));
        public static final long DEFAULT_WIDTH = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("default_width"));
        public static final long DEFAULT_HEIGHT = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("default_height"));
        public static final long THREAD_POOL_CAP = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("thread_pool_cap"));
        public static final long ENGINE_FLAGS = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("engine_flags"));
        public static final long RESOURCES_DIR = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("resources_dir"));
        public static final long CONFIG_DIR = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("config_dir"));

        public static final long GLFW_GET_PROC_ADDRESS = LAYOUT.byteOffset(
                MemoryLayout.PathElement.groupElement("glfw_api"),
                MemoryLayout.PathElement.groupElement("glfw_get_proc_address")
        );
        public static final long GLFW_MAKE_CONTEXT_CURRENT = LAYOUT.byteOffset(
                MemoryLayout.PathElement.groupElement("glfw_api"),
                MemoryLayout.PathElement.groupElement("glfw_make_context_current")
        );
        public static final long GLFW_DEFAULT_WINDOW_HINTS = LAYOUT.byteOffset(
                MemoryLayout.PathElement.groupElement("glfw_api"),
                MemoryLayout.PathElement.groupElement("glfw_default_window_hints")
        );
        public static final long GLFW_WINDOW_HINT = LAYOUT.byteOffset(
                MemoryLayout.PathElement.groupElement("glfw_api"),
                MemoryLayout.PathElement.groupElement("glfw_window_hint")
        );
        public static final long GLFW_GET_WINDOW_ATTRIB = LAYOUT.byteOffset(
                MemoryLayout.PathElement.groupElement("glfw_api"),
                MemoryLayout.PathElement.groupElement("glfw_get_window_attrib")
        );
        public static final long GLFW_CREATE_WINDOW = LAYOUT.byteOffset(
                MemoryLayout.PathElement.groupElement("glfw_api"),
                MemoryLayout.PathElement.groupElement("glfw_create_window")
        );
        public static final long GLFW_DESTROY_WINDOW = LAYOUT.byteOffset(
                MemoryLayout.PathElement.groupElement("glfw_api"),
                MemoryLayout.PathElement.groupElement("glfw_destroy_window")
        );

        private EngineConfig() {
        }

        public static MemorySegment allocate(Arena arena) {
            return arena.allocate(LAYOUT);
        }
    }

    public static final class ViewConfig {
        public static final MemoryLayout LAYOUT = VIEW_CONFIG;
        public static final long SIZE = LAYOUT.byteSize();

        public static final long ENGINE = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("engine"));
        public static final long WIDTH = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("width"));
        public static final long HEIGHT = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("height"));
        public static final long TARGET_FPS = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("target_fps"));
        public static final long VIEW_FLAGS = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("view_flags"));

        private ViewConfig() {
        }

        public static MemorySegment allocate(Arena arena) {
            return arena.allocate(LAYOUT);
        }
    }

    public static final class Frame {
        public static final MemoryLayout LAYOUT = FRAME;
        public static final long SIZE = LAYOUT.byteSize();

        public static final long SLOT = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("slot"));
        public static final long TEXTURE_ID = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("texture_id"));
        public static final long PRODUCER_FENCE = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("producer_fence"));
        public static final long WIDTH = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("width"));
        public static final long HEIGHT = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("height"));

        private Frame() {
        }
    }

    public static final class InputEvent {
        public static final MemoryLayout LAYOUT = INPUT_EVENT;
        public static final long SIZE = LAYOUT.byteSize();

        public static final long KIND = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("kind"));
        public static final long X = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("x"));
        public static final long Y = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("y"));
        public static final long MODIFIERS = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("modifiers"));

        public static final long MOUSE_BUTTON = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("mouse_button"));
        public static final long MOUSE_ACTION = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("mouse_action"));

        public static final long WHEEL_DELTA_X = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("wheel_delta_x"));
        public static final long WHEEL_DELTA_Y = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("wheel_delta_y"));
        public static final long WHEEL_DELTA_Z = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("wheel_delta_z"));
        public static final long WHEEL_MODE = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("wheel_mode"));

        public static final long KEY_STATE = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("key_state"));
        public static final long KEY_LOCATION = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("key_location"));
        public static final long REPEAT = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("repeat"));
        public static final long IS_COMPOSING = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("is_composing"));
        public static final long KEY_CODEPOINT = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("key_codepoint"));
        public static final long GLFW_KEY = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("glfw_key"));

        private InputEvent() {
        }
    }
}
