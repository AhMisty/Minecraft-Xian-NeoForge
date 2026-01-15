package cn.ahmisty.minecraft.xian.mixins.client.gui;

import cn.ahmisty.minecraft.xian.client.gui.WebView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.neoforged.neoforge.client.gui.ModListScreen$InfoPanel", remap = false)
public abstract class ModListScreenMixin_InfoPanel extends ScrollPanel {
    private static final int XIAN_PADDING = 6; // Matches ModListScreen.PADDING
    private static final int XIAN_SCROLLBAR_WIDTH = 6; // ScrollPanel default scrollbar width

    @Unique
    private static WebView xian$webview;

    // Dummy constructor for compilation; mixins are never instantiated directly.
    protected ModListScreenMixin_InfoPanel(Minecraft client, int width, int height, int top, int left) {
        super(client, width, height, top, left);
    }

    @Inject(method = "<init>", at = @At("HEAD"))
    private static void xian$init(ModListScreen this$0, Minecraft mcIn, int widthIn, int heightIn, int topIn, CallbackInfo ci) throws Throwable {
        int viewWidth = Math.max(1, widthIn - (XIAN_PADDING * 2) - XIAN_SCROLLBAR_WIDTH);
        int viewHeight = Math.max(1, heightIn - (XIAN_PADDING * 2));
        // Use a data: URL first to validate the render/texture plumbing without relying on network/TLS.
        xian$webview = new WebView(
                0,
                0,
                viewWidth,
                viewHeight,
                1,
                "data:text/html,<meta charset=utf-8><style>body{margin:0;background:#0f0;font-family:sans-serif;}h1{margin:12px;font-size:28px;}</style><h1>Servo WebView OK</h1>"
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (xian$webview == null) {
            return;
        }

        int x = this.left + XIAN_PADDING;
        int y = this.top + XIAN_PADDING;
        int w = Math.max(1, this.width - (XIAN_PADDING * 2) - XIAN_SCROLLBAR_WIDTH);
        int h = Math.max(1, this.height - (XIAN_PADDING * 2));

        xian$webview.render(guiGraphics, x, y, w, h, mouseX, mouseY, partialTick);
    }
}
