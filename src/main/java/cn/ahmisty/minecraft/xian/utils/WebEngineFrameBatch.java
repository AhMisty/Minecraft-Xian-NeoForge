package cn.ahmisty.minecraft.xian.utils;

import cn.ahmisty.minecraft.xian.ffi.Types;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

public final class WebEngineFrameBatch implements AutoCloseable {
    private final Arena arena;
    private final boolean ownsArena;
    private final int capacity;

    private final MemorySegment views;
    private final MemorySegment outViewIndices;
    private final MemorySegment outFrames;
    private final MemorySegment slots;
    private final MemorySegment consumerFences;

    public WebEngineFrameBatch(int capacity) {
        this(Arena.ofConfined(), true, capacity);
    }

    public WebEngineFrameBatch(Arena arena, int capacity) {
        this(arena, false, capacity);
    }

    private WebEngineFrameBatch(Arena arena, boolean ownsArena, int capacity) {
        this.arena = Objects.requireNonNull(arena, "arena");
        this.ownsArena = ownsArena;
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be >= 0");
        }
        this.capacity = capacity;

        this.views = arena.allocate(Types.U64, capacity);
        this.outViewIndices = arena.allocate(Types.U32, capacity);
        this.outFrames = arena.allocate(WebEngineAbi.FRAME, capacity);
        this.slots = arena.allocate(Types.U32, capacity);
        this.consumerFences = arena.allocate(Types.U64, capacity);
    }

    public int capacity() {
        return capacity;
    }

    public Arena arena() {
        return arena;
    }

    public MemorySegment views() {
        return views;
    }

    public long viewsAddress() {
        return views.address();
    }

    public MemorySegment outViewIndices() {
        return outViewIndices;
    }

    public long outViewIndicesAddress() {
        return outViewIndices.address();
    }

    public MemorySegment outFrames() {
        return outFrames;
    }

    public long outFramesAddress() {
        return outFrames.address();
    }

    public MemorySegment slots() {
        return slots;
    }

    public long slotsAddress() {
        return slots.address();
    }

    public MemorySegment consumerFences() {
        return consumerFences;
    }

    public long consumerFencesAddress() {
        return consumerFences.address();
    }

    public void setView(int index, long view) {
        if (index < 0 || index >= capacity) {
            throw new IndexOutOfBoundsException(index);
        }
        views.set(Types.U64, ((long) index) << 3, view);
    }

    public void setConsumerFence(int index, long fence) {
        if (index < 0 || index >= capacity) {
            throw new IndexOutOfBoundsException(index);
        }
        consumerFences.set(Types.U64, ((long) index) << 3, fence);
    }

    public void setSlot(int index, int slot) {
        if (index < 0 || index >= capacity) {
            throw new IndexOutOfBoundsException(index);
        }
        slots.set(Types.U32, ((long) index) << 2, slot);
    }

    @Override
    public void close() {
        if (ownsArena) {
            arena.close();
        }
    }
}
