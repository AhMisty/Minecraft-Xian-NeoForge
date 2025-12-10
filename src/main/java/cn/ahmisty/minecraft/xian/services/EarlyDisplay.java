package cn.ahmisty.minecraft.xian.services;

import com.google.auto.service.AutoService;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.ProgramArgs;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import net.neoforged.neoforgespi.earlywindow.ImmediateWindowProvider;
import net.neoforged.neoforgespi.locating.IOrderedProvider;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;

@AutoService({
        GraphicsBootstrapper.class,
        ImmediateWindowProvider.class
})
public class EarlyDisplay implements GraphicsBootstrapper, ImmediateWindowProvider, IOrderedProvider {
    public static final String NAME = "cn/ahmisty/minecraft/xian";
    private static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    private static ImmediateWindowProvider PROVIDER;

    @Override
    public String name () {
        if (PROVIDER != null) {
            return PROVIDER.name();
        }
        return NAME;
    }

    @Override
    public int getPriority() {
        if (PROVIDER != null) {
            return Integer.MAX_VALUE;
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public void bootstrap(String[] arguments) {
        getProvider();
        cleanGhostProgressBars();
        proxyGlfw();
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

    public static class glfwSwapBuffers {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(0) long window) {
        }
    }
    public static void proxyGlfw () {
        ByteBuddyAgent.install();
        new ByteBuddy()
                .redefine(GLFW.class)
                .visit(Advice.to(glfwSwapBuffers.class).on(
                        net.bytebuddy.matcher.ElementMatchers.named("glfwSwapBuffers")
                ))
                .make()
                .load(GLFW.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
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

    @Override
    public void initialize(ProgramArgs args) {
        LOGGER.info("Use provider: {}", PROVIDER.name());
        PROVIDER.initialize(args);
    }
    @Override
    public void setMinecraftVersion(String version) {
        PROVIDER.setMinecraftVersion(version);
    }
    @Override
    public void setNeoForgeVersion(String version) {
        PROVIDER.setNeoForgeVersion(version);
    }
    @Override
    public void crash(String message) {
        PROVIDER.crash(message);
    }
    @Override
    public void displayFatalErrorAndExit(List<ModLoadingIssue> issues, @Nullable Path modsFolder, @Nullable Path logFile, @Nullable Path crashReportFile) {
        PROVIDER.displayFatalErrorAndExit(issues, modsFolder, logFile, crashReportFile);
    }
    @Override
    public long takeOverGlfwWindow() {
        return PROVIDER.takeOverGlfwWindow();
    }
    @Override
    public void periodicTick() {
        PROVIDER.periodicTick();
    }
    @Override
    public void updateProgress(String label) {
        PROVIDER.updateProgress(label);
    }
    @Override
    public void completeProgress() {
        PROVIDER.completeProgress();
    }
}