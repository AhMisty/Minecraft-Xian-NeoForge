package cn.ahmisty.minecraft.xian.mixins.client.gui;

import cn.ahmisty.minecraft.xian.client.gui.WebView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.neoforged.neoforge.client.gui.ModListScreen$InfoPanel", remap = false)
public abstract class ModListScreenMixin_InfoPanel extends ScrollPanel {

    @Unique
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("WebView");

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("xian");

    @Unique
    private static WebView xian$webview;

    @Unique
    private static boolean xian$webviewInitFailed;

    protected ModListScreenMixin_InfoPanel(Minecraft client, int width, int height, int top, int left) {
        super(client, width, height, top, left);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (xian$webview == null) {
            if (xian$webviewInitFailed) {
                return;
            }
            try {
                // Use a data: URL first to validate the render/texture plumbing without relying on network/TLS.
                xian$webview = new WebView(
                        0,
                        0,
                        this.width,
                        this.height,
                        4,
                        // Use base64 to avoid any data: URL decoding edge-cases.
                        "https://example.com/"
                );
                LOGGER.info(LOGGERMARKER, "Created WebView for ModList InfoPanel (texture_id={})", xian$webview.texture_id);
            } catch (Throwable t) {
                xian$webview = null;
                xian$webviewInitFailed = true;
                LOGGER.error(LOGGERMARKER, "Failed to create WebView for ModList InfoPanel", t);
                return;
            }
        }

        xian$webview.render(guiGraphics, this.left, this.top, this.width, this.height, mouseX, mouseY, partialTick);
    }
}
