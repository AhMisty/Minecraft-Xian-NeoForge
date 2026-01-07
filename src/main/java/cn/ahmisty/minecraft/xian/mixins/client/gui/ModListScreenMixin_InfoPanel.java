package cn.ahmisty.minecraft.xian.mixins.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.neoforged.neoforge.client.gui.ModListScreen$InfoPanel", remap = false)
public class ModListScreenMixin_InfoPanel {
    @Inject(method = "drawPanel", at = @At("HEAD"), remap = false, cancellable = true)
    private void xian$drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, int mouseX, int mouseY, CallbackInfo ci) {
        ci.cancel();
    }
}
