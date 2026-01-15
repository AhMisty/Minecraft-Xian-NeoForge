package cn.ahmisty.minecraft.xian.mixins.client.gui;

import cn.ahmisty.minecraft.xian.client.gui.WebView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.neoforged.neoforge.client.gui.ModListScreen$InfoPanel", remap = false)
public abstract class ModListScreenMixin_InfoPanel extends ScrollPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModListScreenMixin_InfoPanel.class);
    private static final int XIAN_PADDING = 6; // Matches ModListScreen.PADDING
    private static final int XIAN_SCROLLBAR_WIDTH = 6; // ScrollPanel default scrollbar width
    private static final String INITIAL_URL = "data:text/html,<meta charset=utf-8><style>body{margin:0;background:#0f0;font-family:sans-serif;}h1{margin:12px;font-size:28px;}</style><h1>Servo WebView OK</h1>";

    @Unique
    private static WebView xian$webview;

    // Dummy constructor for compilation; mixins are never instantiated directly.
    protected ModListScreenMixin_InfoPanel(Minecraft client, int width, int height, int top, int left) {
        super(client, width, height, top, left);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int x = this.left + XIAN_PADDING;
        int y = this.top + XIAN_PADDING;
        int w = Math.max(1, this.width - (XIAN_PADDING * 2) - XIAN_SCROLLBAR_WIDTH);
        int h = Math.max(1, this.height - (XIAN_PADDING * 2));

        if (xian$webview == null) {
            try {
                // Use a data: URL first to validate the render/texture plumbing without relying on network/TLS.
                xian$webview = new WebView(0, 0, w, h, 1, INITIAL_URL);
            } catch (Throwable t) {
                LOGGER.warn("Failed to create xian WebView (w={}, h={})", w, h, t);
                xian$webview = null;
                return;
            }
        }

        xian$webview.render(guiGraphics, x, y, w, h, mouseX, mouseY, partialTick);
    }
}
