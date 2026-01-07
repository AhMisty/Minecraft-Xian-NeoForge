package cn.ahmisty.minecraft.xian.services.early;

import com.google.auto.service.AutoService;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.ProgramArgs;
import net.neoforged.neoforgespi.earlywindow.ImmediateWindowProvider;
import net.neoforged.neoforgespi.locating.IOrderedProvider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.nio.file.Path;
import java.util.List;

@AutoService({ImmediateWindowProvider.class})
public class Provider implements ImmediateWindowProvider, IOrderedProvider {
    private static final String NAME = "仙";
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("Early/Provider");
    private static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    @Override
    public String name () {
        if (Bootstrapper.PROVIDER == null) {
            return NAME;
        } else {
            return Bootstrapper.PROVIDER.name();
        }
    }

    @Override
    public int getPriority() {return Integer.MAX_VALUE;}

    @Override
    public void initialize(ProgramArgs args) {
        LOGGER.info(LOGGERMARKER, "Use provider: {}", Bootstrapper.PROVIDER.name());
        Bootstrapper.PROVIDER.initialize(args);
    }
    @Override
    public void setMinecraftVersion(String version) {
        Bootstrapper.PROVIDER.setMinecraftVersion(version);
    }
    @Override
    public void setNeoForgeVersion(String version) {
        Bootstrapper.PROVIDER.setNeoForgeVersion(version);
    }
    @Override
    public void crash(String message) {
        Bootstrapper.PROVIDER.crash(message);
    }
    @Override
    public void displayFatalErrorAndExit(List<ModLoadingIssue> issues, @Nullable Path modsFolder, @Nullable Path logFile, @Nullable Path crashReportFile) {
        Bootstrapper.PROVIDER.displayFatalErrorAndExit(issues, modsFolder, logFile, crashReportFile);
    }
    @Override
    public long takeOverGlfwWindow() {
        long glfwWindow = Bootstrapper.PROVIDER.takeOverGlfwWindow();
        System.setProperty("GLFW_WINDOW_DEFAULT", String.valueOf(glfwWindow));
        return glfwWindow;
    }
    @Override
    public void periodicTick() {
        Bootstrapper.PROVIDER.periodicTick();
    }
    @Override
    public void updateProgress(String label) {
        Bootstrapper.PROVIDER.updateProgress(label);
    }
    @Override
    public void completeProgress() {
        Bootstrapper.PROVIDER.completeProgress();
    }
}
