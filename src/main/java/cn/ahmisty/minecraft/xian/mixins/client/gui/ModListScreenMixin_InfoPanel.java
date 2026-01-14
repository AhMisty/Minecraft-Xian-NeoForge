package cn.ahmisty.minecraft.xian.mixins.client.gui;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.gui.ModListScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.neoforged.neoforge.client.gui.ModListScreen$InfoPanel", remap = false)
public final class ModListScreenMixin_InfoPanel {
    @Inject(method = "<init>", at = @At("HEAD"))
    private static void xian$init(ModListScreen this$0, Minecraft mcIn, int widthIn, int heightIn, int topIn, CallbackInfo ci) {
    }
}
