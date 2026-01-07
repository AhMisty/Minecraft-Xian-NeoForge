package cn.ahmisty.minecraft.xian.mixins.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 关键点 1: 必须使用 targets 指定内部类全名 (注意中间的 $ 符号)
@Mixin(targets = "net.neoforged.neoforge.client.gui.ModListScreen$InfoPanel")
public class ModListScreenMixin_InfoPanel {
    @Unique
    private static final Logger ModListScreen$LOGGER = LoggerFactory.getLogger("xian");

    // 关键点 2: remap = false (因为这是 NeoForge 自己的方法，不是 Minecraft 原生混淆方法)
    // 关键点 3: 参数必须完全匹配 InfoPanel.drawPanel
    @Inject(method = "drawPanel", at = @At("RETURN"), remap = false)
    private void onDrawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, int mouseX, int mouseY, CallbackInfo ci) {
        // 测试绘制：在左上角画一个红色的 "Mixin Works"
//        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, "Mixin Works!", entryRight - 50, relativeY, 0xFFFF0000);
    }
}