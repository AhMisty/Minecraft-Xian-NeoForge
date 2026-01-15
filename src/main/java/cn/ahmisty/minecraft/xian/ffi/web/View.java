package cn.ahmisty.minecraft.xian.ffi.web;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

public final class View {
    public final MemorySegment handle;
    public int width = 0;
    public int height = 0;

    static {
        Engine.safe_init();
    }

    public View(Config config) throws Throwable {
        MemorySegment view = (MemorySegment) Abi.view_create.invokeExact(config.memory_segment);
        if (view == MemorySegment.NULL) {
            throw new IllegalStateException("Failed to create view");
        }
        handle = view;
    }

    public void destroy() throws Throwable {
        Abi.view_destroy.invokeExact(handle);
    }

    public boolean load_url(String url) throws Throwable {
        if (url == null || url.isEmpty()) {
            return false;
        }
        try (Arena arena = Arena.ofConfined()) {
            return (boolean) Abi.view_load_url.invokeExact(handle, arena.allocateFrom(url, StandardCharsets.UTF_8));
        }
    }

    public void resize(int width, int height) throws Throwable {
        this.width = width;
        this.height = height;
        Abi.view_resize.invokeExact(handle, width, height);
    }

    public boolean set_hidpi_scale_factor(float hidpi_scale_factor) throws Throwable {
        return (boolean) Abi.view_set_hidpi_scale_factor.invokeExact(handle, hidpi_scale_factor);
    }

    public int texture_id() throws Throwable {
        return (int) Abi.view_texture_id.invokeExact(handle);
    }

    public boolean needs_paint() throws Throwable {
        return (boolean) Abi.view_needs_paint.invokeExact(handle);
    }

    public boolean paint() throws Throwable {
        return (boolean) Abi.view_paint.invokeExact(handle);
    }

    public int send_input_events(InputEventBuffer events, int count) throws Throwable {
        if (events == null || count <= 0) {
            return 0;
        }
        if (count > events.capacity) {
            throw new IllegalArgumentException("Count must be <= events.capacity");
        }
        return (int) Abi.view_send_input_events.invokeExact(handle, events.slice(count), count);
    }

    public static final class InputEventBuffer {
        private static final long STRIDE = Abi.INPUT_EVENT.byteSize();

        public final MemorySegment memory_segment;
        public final int capacity;

        public InputEventBuffer(Arena arena, int capacity) {
            this.capacity = capacity;
            memory_segment = arena.allocate(MemoryLayout.sequenceLayout(capacity, Abi.INPUT_EVENT));
        }

        public MemorySegment slice(int count) {
            return memory_segment.asSlice(0, STRIDE * (long) count);
        }

        public void set_kind(int index, int kind) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_KIND, kind);
        }

        public void set_x(int index, float x) {
            memory_segment.set(ValueLayout.JAVA_FLOAT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_X, x);
        }

        public void set_y(int index, float y) {
            memory_segment.set(ValueLayout.JAVA_FLOAT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_Y, y);
        }

        public void set_modifiers(int index, int modifiers) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_MODIFIERS, modifiers);
        }

        public void set_mouse_button(int index, int mouse_button) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_MOUSE_BUTTON, mouse_button);
        }

        public void set_mouse_action(int index, int mouse_action) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_MOUSE_ACTION, mouse_action);
        }

        public void set_wheel_delta_x(int index, double wheel_delta_x) {
            memory_segment.set(ValueLayout.JAVA_DOUBLE, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_WHEEL_DELTA_X, wheel_delta_x);
        }

        public void set_wheel_delta_y(int index, double wheel_delta_y) {
            memory_segment.set(ValueLayout.JAVA_DOUBLE, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_WHEEL_DELTA_Y, wheel_delta_y);
        }

        public void set_wheel_delta_z(int index, double wheel_delta_z) {
            memory_segment.set(ValueLayout.JAVA_DOUBLE, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_WHEEL_DELTA_Z, wheel_delta_z);
        }

        public void set_wheel_mode(int index, int wheel_mode) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_WHEEL_MODE, wheel_mode);
        }

        public void set_key_state(int index, int key_state) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_KEY_STATE, key_state);
        }

        public void set_key_location(int index, int key_location) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_KEY_LOCATION, key_location);
        }

        public void set_repeat(int index, int repeat) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_REPEAT, repeat);
        }

        public void set_is_composing(int index, int is_composing) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_IS_COMPOSING, is_composing);
        }

        public void set_key_codepoint(int index, int key_codepoint) {
            memory_segment.set(ValueLayout.JAVA_INT, STRIDE * (long) index + Abi.OFFSET_INPUT_EVENT_KEY_CODEPOINT, key_codepoint);
        }

        public void set_glfw_key(int index, int glfw_key) {
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

        public Config set_width(int width) {
            memory_segment.set(ValueLayout.JAVA_INT, Abi.OFFSET_VIEW_CONFIG_WIDTH, width);
            return this;
        }

        public Config set_height(int height) {
            memory_segment.set(ValueLayout.JAVA_INT, Abi.OFFSET_VIEW_CONFIG_HEIGHT, height);
            return this;
        }

        public Config set_hidpi_scale_factor(float hidpi_scale_factor) {
            memory_segment.set(ValueLayout.JAVA_FLOAT, Abi.OFFSET_VIEW_CONFIG_HIDPI_SCALE_FACTOR, hidpi_scale_factor);
            return this;
        }

        public Config set_initial_url(String initial_url) {
            if (initial_url == null || initial_url.isEmpty()) {
                memory_segment.set(ValueLayout.ADDRESS, Abi.OFFSET_VIEW_CONFIG_INITIAL_URL, MemorySegment.NULL);
            } else {
                memory_segment.set(ValueLayout.ADDRESS, Abi.OFFSET_VIEW_CONFIG_INITIAL_URL, arena.allocateFrom(initial_url, StandardCharsets.UTF_8));
            }
            return this;
        }
    }
}
