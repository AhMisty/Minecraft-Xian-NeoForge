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

    @Inject(method = "drawPanel", at = @At("HEAD"), remap = false, cancellable = true)
    private void onDrawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, int mouseX, int mouseY, CallbackInfo ci) {

        // 1. 在这里写你自己的逻辑
        ModListScreen$LOGGER.info("正在执行自定义的 drawPanel 逻辑");

        // 示例：在这里自己绘制一些东西
        // guiGraphics.drawString(...)

        // 2. 执行取消，这会阻止原版代码运行 (相当于 return)
        ci.cancel();
    }
}