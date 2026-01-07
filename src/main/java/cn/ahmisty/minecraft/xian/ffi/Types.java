package cn.ahmisty.minecraft.xian.ffi;

import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;

public final class Types {
    public static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

    public static final ValueLayout.OfByte U8 = ValueLayout.JAVA_BYTE;
    public static final ValueLayout.OfBoolean BOOL = ValueLayout.JAVA_BOOLEAN;
    public static final ValueLayout.OfInt U32 = ValueLayout.JAVA_INT.withOrder(NATIVE_ORDER);
    public static final ValueLayout.OfLong U64 = ValueLayout.JAVA_LONG.withOrder(NATIVE_ORDER);
    public static final ValueLayout.OfFloat F32 = ValueLayout.JAVA_FLOAT.withOrder(NATIVE_ORDER);
    public static final ValueLayout.OfDouble F64 = ValueLayout.JAVA_DOUBLE.withOrder(NATIVE_ORDER);

    static {
        long pointerSize = ValueLayout.ADDRESS.byteSize();
        if (pointerSize != Long.BYTES) {
            throw new IllegalStateException("Types requires 64-bit pointers (pointerSize=" + pointerSize + ")");
        }
    }

    private Types() {
    }
}
