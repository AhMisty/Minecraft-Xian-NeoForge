package cn.ahmisty.minecraft.xian.ffi;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class Strings {
    public static final Charset UTF8 = StandardCharsets.UTF_8;

    private Strings() {
    }

    public static MemorySegment utf8z(Arena arena, String value) {
        Objects.requireNonNull(arena, "arena");
        if (value == null) {
            return MemorySegment.NULL;
        }
        return arena.allocateFrom(value, UTF8);
    }

    public static long utf8zAddress(Arena arena, String value) {
        MemorySegment segment = utf8z(arena, value);
        return segment == MemorySegment.NULL ? 0L : segment.address();
    }

    public static MemorySegment utf8zOrNullIfBlank(Arena arena, String value) {
        Objects.requireNonNull(arena, "arena");
        if (value == null || value.isBlank()) {
            return MemorySegment.NULL;
        }
        return arena.allocateFrom(value, UTF8);
    }

    public static long utf8zOrNullIfBlankAddress(Arena arena, String value) {
        MemorySegment segment = utf8zOrNullIfBlank(arena, value);
        return segment == MemorySegment.NULL ? 0L : segment.address();
    }
}
