package cn.ahmisty.minecraft.xian.ffi.web.engine;

import java.lang.foreign.Arena;

public class Config {
    public final Arena ARENA = Arena.ofConfined();

    public Config() {
        ARENA.allocate(Abi.Native.Struct.CONFIG);
    }
}
;