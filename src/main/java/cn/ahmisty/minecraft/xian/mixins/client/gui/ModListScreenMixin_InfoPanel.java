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

    @Unique
    private static WebView xian$webview;

    // Dummy constructor for compilation; mixins are never instantiated directly.
    protected ModListScreenMixin_InfoPanel(Minecraft client, int width, int height, int top, int left) {
        super(client, width, height, top, left);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (xian$webview == null) {
            try {
                // Use a data: URL first to validate the render/texture plumbing without relying on network/TLS.
                xian$webview = new WebView(0, 0, this.width, this.height, 1, "http://baidu.com");
            } catch (Throwable t) {
                xian$webview = null;
                return;
            }
        }

        xian$webview.render(guiGraphics, this.left, this.top, this.width, this.height, mouseX, mouseY, partialTick);
    }
}
