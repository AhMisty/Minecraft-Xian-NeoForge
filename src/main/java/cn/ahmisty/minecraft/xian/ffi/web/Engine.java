package cn.ahmisty.minecraft.xian.ffi.web;

import net.neoforged.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class Engine {
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("WebEngine");
    private static final Logger LOGGER = LoggerFactory.getLogger("仙");

    private Engine() {
    }

    public static final Path WORKSPACE = FMLPaths.GAMEDIR.get().resolve("xian").resolve("web");

    private static boolean dirHasAnyEntries(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.findAny().isPresent();
        } catch (IOException e) {
            return false;
        }
    }

    static {
        try (Arena arena = Arena.ofConfined()) {
            // NOTE: Setting a directory-backed Servo ResourceReader to an empty/nonexistent directory will
            // effectively make all resources "missing" and may lead to a fully transparent output.
            // Only override the resource dir when it is actually populated.
            Path resourcesDir = WORKSPACE.resolve("resources");
            if (Files.isDirectory(resourcesDir) && dirHasAnyEntries(resourcesDir)) {
                if (
                        (boolean) Abi.set_resources_dir.invokeExact(
                                arena.allocateFrom(
                                        resourcesDir.toString(),
                                        StandardCharsets.UTF_8
                                )
                        )
                ) {
                    LOGGER.info(LOGGERMARKER, "Successfully set resources dir");
                } else {
                    String error = "Failed to set resources dir";
                    LOGGER.error(LOGGERMARKER, error);
                    throw new ExceptionInInitializerError(error);
                }
            } else {
                LOGGER.warn(LOGGERMARKER, "Skip set resources dir because it is missing/empty: {}", resourcesDir);
            }

            if (
                    (boolean) Abi.set_config_dir.invokeExact(
                            arena.allocateFrom(
                                    WORKSPACE.resolve("config").toString(),
                                    StandardCharsets.UTF_8
                            )
                    )
            ) {
                LOGGER.info(LOGGERMARKER, "Successfully set config dir");
            } else {
                String error = "Failed to set config dir";
                LOGGER.error(LOGGERMARKER, error);
                throw new ExceptionInInitializerError(error);
            }

            if (
                    (boolean) Abi.set_web_root_dir.invokeExact(
                            arena.allocateFrom(
                                    WORKSPACE.toString(),
                                    StandardCharsets.UTF_8
                            )
                    )
            ) {
                LOGGER.info(LOGGERMARKER, "Successfully set web root dir");
            } else {
                String error = "Failed to set web root dir";
                LOGGER.error(LOGGERMARKER, error);
                throw new ExceptionInInitializerError(error);
            }

            if (
                    (boolean) Abi.set_thread_pool_cap.invokeExact(0)
            ) {
                LOGGER.info(LOGGERMARKER, "Successfully set thread pool cap");
            } else {
                String error = "Failed to set thread pool cap";
                LOGGER.error(LOGGERMARKER, error);
                throw new ExceptionInInitializerError(error);
            }

            MemorySegment glfw_api = arena.allocate(Abi.GLFW_API);
            glfw_api.set(ValueLayout.ADDRESS, Abi.OFFSET_GLFW_API_GET_PROC_ADDRESS, MemorySegment.ofAddress(GLFW.Functions.GetProcAddress));
            if (
                    (boolean) Abi.set_glfw_api.invokeExact(glfw_api)
            ) {
                LOGGER.info(LOGGERMARKER, "Successfully set glfw api");
            } else {
                String error = "Failed to set glfw api";
                LOGGER.error(LOGGERMARKER, error);
                throw new ExceptionInInitializerError(error);
            }

            if (
                    (boolean) Abi.set_gl_api.invokeExact(Abi.XIAN_WEB_ENGINE_GL_API_GL)
            ) {
                LOGGER.info(LOGGERMARKER, "Successfully set gl api");
            } else {
                String error = "Failed to set gl api";
                LOGGER.error(LOGGERMARKER, error);
                throw new ExceptionInInitializerError(error);
            }

            LOGGER.info(LOGGERMARKER, "Successfully configured");

            if (
                    (boolean) Abi.init.invokeExact()
            ) {
                LOGGER.info(LOGGERMARKER, "Successfully initialized");
            } else {
                String error = "Failed to initialize";
                LOGGER.error(LOGGERMARKER, error);
                throw new ExceptionInInitializerError(error);
            }
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public static void safe_init() {
    }

    public static int tick() throws Throwable {
        return (int) Abi.tick.invokeExact();
    }
}

