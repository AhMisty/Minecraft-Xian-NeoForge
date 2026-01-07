package cn.ahmisty.minecraft.xian.ffi.web.engine;

import cn.ahmisty.minecraft.xian.ffi.Types;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

public final class View implements AutoCloseable {
    private static final long FRAME_SLOT_OFFSET =
            Abi.FRAME.byteOffset(MemoryLayout.PathElement.groupElement("slot"));
    private static final long FRAME_TEXTURE_ID_OFFSET =
            Abi.FRAME.byteOffset(MemoryLayout.PathElement.groupElement("texture_id"));
    private static final long FRAME_PRODUCER_FENCE_OFFSET =
            Abi.FRAME.byteOffset(MemoryLayout.PathElement.groupElement("producer_fence"));
    private static final long FRAME_WIDTH_OFFSET =
            Abi.FRAME.byteOffset(MemoryLayout.PathElement.groupElement("width"));
    private static final long FRAME_HEIGHT_OFFSET =
            Abi.FRAME.byteOffset(MemoryLayout.PathElement.groupElement("height"));

    private static final long VIEW_CONFIG_ENGINE_OFFSET =
            Abi.VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("engine"));
    private static final long VIEW_CONFIG_WIDTH_OFFSET =
            Abi.VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("width"));
    private static final long VIEW_CONFIG_HEIGHT_OFFSET =
            Abi.VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("height"));
    private static final long VIEW_CONFIG_TARGET_FPS_OFFSET =
            Abi.VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("target_fps"));
    private static final long VIEW_CONFIG_VIEW_FLAGS_OFFSET =
            Abi.VIEW_CONFIG.byteOffset(MemoryLayout.PathElement.groupElement("view_flags"));

    private final Engine engine;
    private long handle;

    private final Arena frameArena;
    private final MemorySegment frameViews;
    private final MemorySegment frameViewIndices;
    private final MemorySegment frameOut;
    private final MemorySegment releaseSlots;
    private final MemorySegment releaseConsumerFences;

    View(Engine engine, Config config) {
        this.engine = engine;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cfg = arena.allocate(Abi.VIEW_CONFIG);
            Abi.view_config_init.invokeExact(cfg.address());

            cfg.set(Types.U64, VIEW_CONFIG_ENGINE_OFFSET, engine.handle());
            cfg.set(Types.U32, VIEW_CONFIG_WIDTH_OFFSET, config.width);
            cfg.set(Types.U32, VIEW_CONFIG_HEIGHT_OFFSET, config.height);
            cfg.set(Types.U32, VIEW_CONFIG_TARGET_FPS_OFFSET, config.targetFps);
            cfg.set(Types.U32, VIEW_CONFIG_VIEW_FLAGS_OFFSET, config.viewFlags);

            handle = (long) Abi.view_create.invokeExact(cfg.address());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        if (handle == 0) {
            throw new IllegalStateException("xian_web_engine_view_create returned NULL");
        }

        frameArena = Arena.ofConfined();
        frameViews = frameArena.allocateFrom(Types.U64, 1);
        frameViewIndices = frameArena.allocateFrom(Types.U32, 1);
        frameOut = frameArena.allocate(Abi.FRAME);
        releaseSlots = frameArena.allocateFrom(Types.U32, 1);
        releaseConsumerFences = frameArena.allocateFrom(Types.U64, 1);
        frameViews.set(Types.U64, 0, handle);
    }

    public long handle() {
        return handle;
    }

    public Engine engine() {
        return engine;
    }

    public void setActive(boolean active) {
        try {
            Abi.view_set_active.invokeExact(handle, (byte) (active ? 1 : 0));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public boolean loadUrl(String url) {
        if (url == null) {
            return false;
        }
        try (Arena arena = Arena.ofConfined()) {
            try {
                return (boolean) Abi.view_load_url.invokeExact(handle, arena.allocateFrom(url, StandardCharsets.UTF_8).address());
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    public void resize(int width, int height) {
        try {
            Abi.view_resize.invokeExact(handle, width, height);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public int sendInputEvents(MemorySegment events, int count) {
        if (handle == 0 || count <= 0 || events == null || events == MemorySegment.NULL) {
            return 0;
        }
        try {
            return (int) Abi.view_send_input_events.invokeExact(
                    handle,
                    events.address(),
                    count
            );
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public boolean acquireFrame(Frame out) {
        if (out == null) {
            throw new NullPointerException("out");
        }

        if (handle == 0) {
            return false;
        }

        try {
            int acquired = (int) Abi.views_acquire_frames.invokeExact(
                    frameViews.address(),
                    frameViewIndices.address(),
                    frameOut.address(),
                    1
            );
            if (acquired == 0) {
                return false;
            }

            out.slot = frameOut.get(Types.U32, FRAME_SLOT_OFFSET);
            out.textureId = frameOut.get(Types.U32, FRAME_TEXTURE_ID_OFFSET);
            out.producerFence = frameOut.get(Types.U64, FRAME_PRODUCER_FENCE_OFFSET);
            out.width = frameOut.get(Types.U32, FRAME_WIDTH_OFFSET);
            out.height = frameOut.get(Types.U32, FRAME_HEIGHT_OFFSET);
            return true;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void releaseFrame(int slot) {
        if (handle == 0) {
            return;
        }

        releaseSlots.set(Types.U32, 0, slot);
        try {
            Abi.views_release_frames.invokeExact(frameViews.address(), releaseSlots.address(), 0L, 1);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void releaseFrame(Frame frame) {
        if (frame == null) {
            throw new NullPointerException("frame");
        }
        releaseFrame(frame.slot);
    }

    public void releaseFrame(int slot, long consumerFence) {
        if (handle == 0) {
            return;
        }

        releaseSlots.set(Types.U32, 0, slot);
        releaseConsumerFences.set(Types.U64, 0, consumerFence);
        try {
            Abi.views_release_frames.invokeExact(
                    frameViews.address(),
                    releaseSlots.address(),
                    releaseConsumerFences.address(),
                    1
            );
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void releaseFrame(Frame frame, long consumerFence) {
        if (frame == null) {
            throw new NullPointerException("frame");
        }
        releaseFrame(frame.slot, consumerFence);
    }

    public void destroy() {
        long ptr = handle;
        if (ptr == 0) {
            return;
        }
        handle = 0;
        try {
            Abi.view_destroy.invokeExact(ptr);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            frameArena.close();
        }
    }

    @Override
    public void close() {
        destroy();
    }

    public static final class Config {
        public int width = 0;
        public int height = 0;
        public int targetFps = 0;
        public int viewFlags = 0;

        public Config size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Config targetFps(int targetFps) {
            this.targetFps = targetFps;
            return this;
        }

        public Config viewFlags(int viewFlags) {
            this.viewFlags = viewFlags;
            return this;
        }
    }

    public static final class Frame {
        public int slot;
        public int textureId;
        public long producerFence;
        public int width;
        public int height;
    }
}
