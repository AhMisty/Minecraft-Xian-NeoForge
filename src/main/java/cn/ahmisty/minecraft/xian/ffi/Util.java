package cn.ahmisty.minecraft.xian.ffi;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public final class Util {
    private Util() {
    }

    public static long address(MemorySegment segment) {
        if (segment == null || segment == MemorySegment.NULL) {
            return 0L;
        }
        return segment.address();
    }

    public static MethodHandle require(MethodHandle handle, String symbol) {
        if (handle == null) {
            throw new UnsatisfiedLinkError("Missing required symbol: " + symbol);
        }
        return handle;
    }

    public static RuntimeException rethrow(Throwable t) {
        if (t instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        if (t instanceof Error error) {
            throw error;
        }
        return new RuntimeException(t);
    }
}
