package cn.ahmisty.minecraft.xian.mixins.client;

import net.neoforged.neoforge.client.gui.ModListScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(ModListScreen.class)
public class ModListScreenMixin {

//    @Inject(method = "drawPanel", at = @At("TAIL"))
//    private void onInit(CallbackInfo ci) {
//        System.out.println("NeoForge Mixin 正在工作！主菜单已初始化。");
//    }
}
