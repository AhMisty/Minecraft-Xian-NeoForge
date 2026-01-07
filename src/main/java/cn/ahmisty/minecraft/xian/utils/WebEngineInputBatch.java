package cn.ahmisty.minecraft.xian.utils;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

public final class WebEngineInputBatch implements AutoCloseable {
    private final Arena arena;
    private final boolean ownsArena;
    private final int capacity;
    private final MemorySegment events;

    public WebEngineInputBatch(int capacity) {
        this(Arena.ofConfined(), true, capacity);
    }

    public WebEngineInputBatch(Arena arena, int capacity) {
        this(arena, false, capacity);
    }

    private WebEngineInputBatch(Arena arena, boolean ownsArena, int capacity) {
        this.arena = Objects.requireNonNull(arena, "arena");
        this.ownsArena = ownsArena;
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be >= 0");
        }
        this.capacity = capacity;
        this.events = arena.allocate(WebEngineAbi.INPUT_EVENT, capacity);
    }

    public int capacity() {
        return capacity;
    }

    public Arena arena() {
        return arena;
    }

    public MemorySegment events() {
        return events;
    }

    public long eventsAddress() {
        return events.address();
    }

    public long eventOffset(int index) {
        if (index < 0 || index >= capacity) {
            throw new IndexOutOfBoundsException(index);
        }
        return (long) index * WebEngineAbi.InputEvent.SIZE;
    }

    @Override
    public void close() {
        if (ownsArena) {
            arena.close();
        }
    }
}
