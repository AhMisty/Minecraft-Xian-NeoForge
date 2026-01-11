package cn.ahmisty.minecraft.xian.ffi.web.engine;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

public final class View {
    public final MemorySegment handle;
    public boolean is_active = true;
    public int width = 0;
    public int height = 0;

    public View(Config config) throws Throwable {
        handle = (MemorySegment) Abi.view_create.invokeExact(config.memory_segment);
    }

    public final void destroy() throws Throwable {
        Abi.view_destroy.invokeExact(handle);
    }

    public final void set_active(boolean is_active) throws Throwable {
        this.is_active = is_active;
        Abi.view_set_active.invokeExact(handle, is_active ? (byte) 1 : (byte) 0);
    }

    public final boolean load_url(String url) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            return (boolean) Abi.view_load_url.invokeExact(handle, arena.allocateFrom(url, StandardCharsets.UTF_8));
        }
    }

    public final void resize(int width, int height) throws Throwable {
        this.width = width;
        this.height = height;
        Abi.view_resize.invokeExact(handle, width, height);
    }

    public final boolean acquire_frame(Frame out_frame) throws Throwable {
        return (boolean) Abi.view_acquire_frame.invokeExact(handle, out_frame.memory_segment);
    }

    public final void release_frame(int slot, long consumer_fence) throws Throwable {
        Abi.view_release_frame.invokeExact(handle, slot, consumer_fence);
    }

    public final int send_input_events(InputEventBuffer events, int count) throws Throwable {
        return (int) Abi.view_send_input_events.invokeExact(handle, events.memory_segment, count);
    }

    public static final class Frame {
        public final MemorySegment memory_segment;

        public Frame(Arena arena) {
            memory_segment = arena.allocate(Abi.FRAME);
        }

        public final long producer_fence() {
            return memory_segment.get(ValueLayout.JAVA_LONG, Abi.OFFSET_FRAME_PRODUCER_FENCE);
        }

        public final int texture_id() {
            return memory_segment.get(ValueLayout.JAVA_INT, Abi.OFFSET_FRAME_TEXTURE_ID);
        }

        public final int slot() {
            return memory_segment.get(ValueLayout.JAVA_INT, Abi.OFFSET_FRAME_SLOT);
        }

        public final int width() {
            return memory_segment.get(ValueLayout.JAVA_INT, Abi.OFFSET_FRAME_WIDTH);
        }

        public final int height() {
            return memory_segment.get(ValueLayout.JAVA_INT, Abi.OFFSET_FRAME_HEIGHT);
        }
    }

    public static final class InputEventBuffer {
        private static final long STRIDE = Abi.INPUT_EVENT.byteSize();

        public final MemorySegment memory_segment;
        public final int capacity;

        public InputEventBuffer(Arena arena, int capacity) {
            this.capacity = capacity;
            memory_segment = arena.allocate(MemoryLayout.sequenceLayout(capacity, Abi.INPUT_EVENT));
        }

        public final MemorySegment slice(int count) {
            return memory_segment.asSlice(0, STRIDE * (long) count);
        }

        public final void set_kind(int index, int kind) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_KIND, kind);
        }

        public final void set_x(int index, float x) {
            memory_segment.set(ValueLayout.JAVA_FLOAT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_X, x);
        }

        public final void set_y(int index, float y) {
            memory_segment.set(ValueLayout.JAVA_FLOAT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_Y, y);
        }

        public final void set_modifiers(int index, int modifiers) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_MODIFIERS, modifiers);
        }

        public final void set_mouse_button(int index, int mouse_button) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_MOUSE_BUTTON, mouse_button);
        }

        public final void set_mouse_action(int index, int mouse_action) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_MOUSE_ACTION, mouse_action);
        }

        public final void set_wheel_delta_x(int index, double wheel_delta_x) {
            memory_segment.set(ValueLayout.JAVA_DOUBLE, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_WHEEL_DELTA_X, wheel_delta_x);
        }

        public final void set_wheel_delta_y(int index, double wheel_delta_y) {
            memory_segment.set(ValueLayout.JAVA_DOUBLE, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_WHEEL_DELTA_Y, wheel_delta_y);
        }

        public final void set_wheel_delta_z(int index, double wheel_delta_z) {
            memory_segment.set(ValueLayout.JAVA_DOUBLE, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_WHEEL_DELTA_Z, wheel_delta_z);
        }

        public final void set_wheel_mode(int index, int wheel_mode) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_WHEEL_MODE, wheel_mode);
        }

        public final void set_key_state(int index, int key_state) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_KEY_STATE, key_state);
        }

        public final void set_key_location(int index, int key_location) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_KEY_LOCATION, key_location);
        }

        public final void set_repeat(int index, int repeat) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_REPEAT, repeat);
        }

        public final void set_is_composing(int index, int is_composing) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_IS_COMPOSING, is_composing);
        }

        public final void set_key_codepoint(int index, int key_codepoint) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_KEY_CODEPOINT, key_codepoint);
        }

        public final void set_glfw_key(int index, int glfw_key) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_GLFW_KEY, glfw_key);
        }
    }

    public static final class Config {
        public final MemorySegment memory_segment;
        public final Arena arena;

        public Config(Arena arena) throws Throwable {
            this.arena = arena;
            memory_segment = arena.allocate(Abi.VIEW_CONFIG);
            Abi.view_config_init.invokeExact(memory_segment);
        }
        public final Config set_engine(Engine engine) {
            memory_segment.set(ValueLayout.ADDRESS, Abi.OFFSET_VIEW_CONFIG_ENGINE, engine.handle);
            return this;
        }
        public final Config set_width(int width) {
            memory_segment.set(ValueLayout.JAVA_INT, Abi.OFFSET_VIEW_CONFIG_WIDTH, width);
            return this;
        }
        public final Config set_height(int height) {
            memory_segment.set(ValueLayout.JAVA_INT, Abi.OFFSET_VIEW_CONFIG_HEIGHT, height);
            return this;
        }
        public final Config set_target_fps(int target_fps) {
            memory_segment.set(ValueLayout.JAVA_INT, Abi.OFFSET_VIEW_CONFIG_TARGET_FPS, target_fps);
            return this;
        }
        public final Config set_view_flags(int view_flags) {
            memory_segment.set(ValueLayout.JAVA_INT, Abi.OFFSET_VIEW_CONFIG_VIEW_FLAGS, view_flags);
            return this;
        }
    }
}
