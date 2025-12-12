package cn.ahmisty.minecraft.xian.services.early;

import com.google.auto.service.AutoService;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.ProgramArgs;
import net.neoforged.neoforgespi.earlywindow.ImmediateWindowProvider;
import net.neoforged.neoforgespi.locating.IOrderedProvider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

@AutoService({ImmediateWindowProvider.class})
public class Provider implements ImmediateWindowProvider, IOrderedProvider {
    private static final String NAME = "Xian/Provider";
    private static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    @Override
    public String name () {
        if (ProviderBootstrapper.PROVIDER == null) {
            return NAME;
        } else {
            return ProviderBootstrapper.PROVIDER.name();
        }
    }

    @Override
    public int getPriority() {return Integer.MAX_VALUE;}

    @Override
    public void initialize(ProgramArgs args) {
        LOGGER.info("Use provider: {}", ProviderBootstrapper.PROVIDER.name());
        ProviderBootstrapper.PROVIDER.initialize(args);
    }
    @Override
    public void setMinecraftVersion(String version) {
        ProviderBootstrapper.PROVIDER.setMinecraftVersion(version);
    }
    @Override
    public void setNeoForgeVersion(String version) {
        ProviderBootstrapper.PROVIDER.setNeoForgeVersion(version);
    }
    @Override
    public void crash(String message) {
        ProviderBootstrapper.PROVIDER.crash(message);
    }
    @Override
    public void displayFatalErrorAndExit(List<ModLoadingIssue> issues, @Nullable Path modsFolder, @Nullable Path logFile, @Nullable Path crashReportFile) {
        ProviderBootstrapper.PROVIDER.displayFatalErrorAndExit(issues, modsFolder, logFile, crashReportFile);
    }
    @Override
    public long takeOverGlfwWindow() {
        return ProviderBootstrapper.PROVIDER.takeOverGlfwWindow();
    }
    @Override
    public void periodicTick() {
        ProviderBootstrapper.PROVIDER.periodicTick();
    }
    @Override
    public void updateProgress(String label) {
        ProviderBootstrapper.PROVIDER.updateProgress(label);
    }
    @Override
    public void completeProgress() {
        ProviderBootstrapper.PROVIDER.completeProgress();
    }
}
