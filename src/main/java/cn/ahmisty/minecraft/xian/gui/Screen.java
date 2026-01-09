package cn.ahmisty.minecraft.xian.gui;

import cn.ahmisty.minecraft.xian.ffi.web.Engine;
import cn.ahmisty.minecraft.xian.ffi.web.View;
import net.minecraft.client.Minecraft;

public final class Screen implements AutoCloseable {
    private static final int ENGINE_FLAGS_MAX_PERF = Engine.FLAG_NO_PARK;
    private static final int VIEW_FLAGS_MAX_PERF = View.FLAG_INPUT_SINGLE_PRODUCER;

    public static final Engine ENGINE = new Engine(new Engine.Config()
            .threadPoolCap(0)
            .engineFlags(Engine.FLAG_NO_PARK));

    private static final class EngineHolder {
        private static final Engine INSTANCE = createEngine();
    }

    private static Engine createEngine() {
        Minecraft minecraft = Minecraft.getInstance();
        long sharedWindow = minecraft.getWindow().handle();
        int width = minecraft.getWindow().getWidth();
        int height = minecraft.getWindow().getHeight();

        return new Engine(new Engine.Config()
                .glfwSharedWindow(sharedWindow)
                .defaultSize(width, height)
                .threadPoolCap(0)
                .engineFlags(ENGINE_FLAGS_MAX_PERF));
    }

    public static Engine engine() {
        return EngineHolder.INSTANCE;
    }

    public static View.Config maxPerfViewConfig() {
        Minecraft minecraft = Minecraft.getInstance();
        return new View.Config()
                .size(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight())
                .targetFps(0)
                .viewFlags(VIEW_FLAGS_MAX_PERF);
    }

    private final View view;

    public Screen() {
        this(maxPerfViewConfig());
    }

    public Screen(View.Config config) {
        view = engine().createView(config);
    }

    public View view() {
        return view;
    }

    public boolean loadUrl(String url) {
        return view.loadUrl(url);
    }

    public void setActive(boolean active) {
        view.setActive(active);
    }

    public void resizeToWindow() {
        Minecraft minecraft = Minecraft.getInstance();
        view.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
    }

    public boolean acquireFrame(View.Frame out) {
        return view.acquireFrame(out);
    }

    public void releaseFrame(int slot) {
        view.releaseFrame(slot);
    }

    public void releaseFrame(int slot, long consumerFence) {
        view.releaseFrame(slot, consumerFence);
    }

    public void destroy() {
        view.destroy();
    }

    @Override
    public void close() {
        destroy();
    }
}
