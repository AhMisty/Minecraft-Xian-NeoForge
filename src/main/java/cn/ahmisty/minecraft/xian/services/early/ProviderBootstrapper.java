package cn.ahmisty.minecraft.xian.services.early;

import com.google.auto.service.AutoService;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import net.neoforged.neoforgespi.earlywindow.ImmediateWindowProvider;
import net.neoforged.neoforgespi.locating.IOrderedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Deque;
import java.util.Objects;
import java.util.ServiceLoader;

@AutoService({GraphicsBootstrapper.class})
public class ProviderBootstrapper implements GraphicsBootstrapper, IOrderedProvider {
    private static final String NAME = "Xian/ProviderBootstrapper";
    private static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static ImmediateWindowProvider PROVIDER;

    @Override
    public String name () {return NAME;}

    @Override
    public int getPriority() {return Integer.MIN_VALUE;}

    @Override
    public void bootstrap(String[] arguments) {
        getProvider();
        cleanGhostProgressBars();
    }

    public static void getProvider () {
        String ProviderName = FMLConfig.getConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_PROVIDER);
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ServiceLoader<ImmediateWindowProvider> serviceLoader = ServiceLoader.load(ImmediateWindowProvider.class, contextClassLoader);
        PROVIDER = serviceLoader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(p -> !Objects.equals(p.name(), NAME))
                .filter(p -> Objects.equals(p.name(), ProviderName))
                .max(Comparator.comparingInt(p -> (p instanceof IOrderedProvider op) ? op.getPriority() : 0))
                .orElse(null);
        if (PROVIDER != null) {
            LOGGER.info("Successfully found provider: {}", PROVIDER.name());
        } else {
            String message = "Could not find provider: " + ProviderName;
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    private static void cleanGhostProgressBars() {
        try {
            Field field = StartupNotificationManager.class.getDeclaredField("progressMeters");
            field.setAccessible(true);
            Deque<?> meters = (Deque<?>) field.get(null);
            if (!meters.isEmpty()) {
                LOGGER.info("Detected {} ghost progress bar(s) created by FML discovery. Clearing...", meters.size());
                meters.clear();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to clean ghost progress bars!", e);
        }
    }
}
