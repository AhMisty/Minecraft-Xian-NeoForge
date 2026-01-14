package cn.ahmisty.minecraft.xian.client.gui;

import cn.ahmisty.minecraft.xian.ffi.web.Engine;
import cn.ahmisty.minecraft.xian.ffi.web.View;

import java.lang.foreign.Arena;

public class WebView {
    static {
        Engine.safe_init();
    }

    public final View view;
    public final int texture_id;
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public WebView(int x, int y, int width, int height, float hidpi_scale_factor, String initial_url) throws Throwable {
        try(Arena arena = Arena.ofConfined()) {
            this.view = new View(
                    new View.Config(arena)
                            .set_width(width)
                            .set_height(height)
                            .set_hidpi_scale_factor(hidpi_scale_factor)
                            .set_initial_url(initial_url)
            );
        }
        this.texture_id = this.view.texture_id();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render() {
    }
}
