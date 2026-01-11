package cn.ahmisty.minecraft.xian.services.early;

import com.google.auto.service.AutoService;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.IOrderedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Set;

//@AutoService({IModFileCandidateLocator.class})
public final class Reload implements IModFileCandidateLocator, IOrderedProvider {
    public static final String NAME = "仙";
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("Reload");
    private static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
        try {
            FMLLoader loader = FMLLoader.getCurrent();
            Path path = Path.of(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            Field locatedPathsField = FMLLoader.class.getDeclaredField("locatedPaths");
            locatedPathsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<Path> locatedPaths = (Set<Path>) locatedPathsField.get(loader);
            boolean removed = locatedPaths.remove(path);
            if (removed) {
                LOGGER.debug(LOGGERMARKER, "Successfully remove locatedPath: {}", path);
            }
        } catch (Exception e) {
            LOGGER.error(LOGGERMARKER, "Could not remove locatedPath.", e);
        }
    }
}
