package cn.ahmisty.minecraft.xian;

import cn.ahmisty.minecraft.xian.ffi.web.Engine;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class WebEngineTicker {
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("WebEngineTicker");
    private static final Logger LOGGER = LoggerFactory.getLogger("仙");

    @SubscribeEvent
    public static void onTick(RenderFrameEvent.Pre e) {
        try {
            Engine.tick();
        } catch (Throwable t) {
            LOGGER.error(LOGGERMARKER, "Failed to tick the WebEngine", t);
        }
    }
}
