package cn.ahmisty.minecraft.xian.utils;

import cn.ahmisty.minecraft.xian.ffi.Strings;
import cn.ahmisty.minecraft.xian.ffi.Util;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

public final class WebEngineView implements AutoCloseable {
    private final WebEngineAbi abi;
    private long handle;

    WebEngineView(WebEngineAbi abi, long engine, Config config) {
        this.abi = Objects.requireNonNull(abi, "abi");
        Objects.requireNonNull(config, "config");

        long created;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cfg = WebEngineAbi.ViewConfig.allocate(arena);
            abi.viewConfigInit(cfg);

            cfg.set(WebEngineAbi.U64, WebEngineAbi.ViewConfig.ENGINE, engine);
            cfg.set(WebEngineAbi.U32, WebEngineAbi.ViewConfig.WIDTH, config.width);
            cfg.set(WebEngineAbi.U32, WebEngineAbi.ViewConfig.HEIGHT, config.height);
            cfg.set(WebEngineAbi.U32, WebEngineAbi.ViewConfig.TARGET_FPS, config.targetFps);
            cfg.set(WebEngineAbi.U32, WebEngineAbi.ViewConfig.VIEW_FLAGS, config.viewFlags);

            created = abi.viewCreate(cfg);
        }

        if (created == 0L) {
            throw new IllegalStateException("xian_web_engine_view_create returned NULL");
        }
        this.handle = created;
    }

    public WebEngineAbi abi() {
        return abi;
    }

    public long handle() {
        return handle;
    }

    public boolean isClosed() {
        return handle == 0L;
    }

    public void setActive(boolean active) {
        long view = handle;
        if (view == 0L) {
            return;
        }
        abi.viewSetActive(view, active);
    }

    public boolean loadUrl(String url) {
        long view = handle;
        if (view == 0L) {
            return false;
        }
        if (url == null) {
            return false;
        }
        try (Arena arena = Arena.ofConfined()) {
            return abi.viewLoadUrl(view, Strings.utf8zAddress(arena, url));
        }
    }

    public boolean loadUrl(MemorySegment urlCString) {
        return loadUrl(Util.address(urlCString));
    }

    public boolean loadUrl(long urlCString) {
        long view = handle;
        if (view == 0L) {
            return false;
        }
        if (urlCString == 0L) {
            return false;
        }
        return abi.viewLoadUrl(view, urlCString);
    }

    public void resize(int width, int height) {
        long view = handle;
        if (view == 0L) {
            return;
        }
        abi.viewResize(view, width, height);
    }

    public int sendInputEvents(long events, int count) {
        long view = handle;
        if (view == 0L) {
            return 0;
        }
        if (events == 0L || count <= 0) {
            return 0;
        }
        return abi.viewSendInputEvents(view, events, count);
    }

    public int sendInputEvents(MemorySegment events, int count) {
        return sendInputEvents(Util.address(events), count);
    }

    public int sendInputEvents(WebEngineInputBatch batch, int count) {
        if (batch == null) {
            return 0;
        }
        return sendInputEvents(batch.eventsAddress(), count);
    }

    @Override
    public void close() {
        long view = handle;
        if (view == 0L) {
            return;
        }
        handle = 0L;
        abi.viewDestroy(view);
    }

    public static final class Config {
        public int width = 1;
        public int height = 1;
        public int targetFps = 0;
        public int viewFlags = 0;
    }
}
