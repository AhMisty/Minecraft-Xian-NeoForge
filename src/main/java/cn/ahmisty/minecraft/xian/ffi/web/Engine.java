package cn.ahmisty.minecraft.xian.ffi.web;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class Engine {
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("WebEngine");
    private static final Logger LOGGER = LoggerFactory.getLogger("仙");

    private Engine() {
    }

    public static final Path WORKSPACE = FMLPaths.GAMEDIR.get().resolve("xian").resolve("web");

    static {
        try (Arena arena = Arena.ofConfined()) {
            if (
                    (boolean) Abi.set_resources_dir.invokeExact(
                            arena.allocateFrom(
                                    WORKSPACE.resolve("resources").toString(),
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
            glfw_api.set(ValueLayout.ADDRESS, Abi.OFFSET_GLFW_API_MAKE_CONTEXT_CURRENT, MemorySegment.ofAddress(GLFW.Functions.MakeContextCurrent));
            if (
                    (boolean) Abi.set_glfw_context.invokeExact(
                            MemorySegment.ofAddress(Minecraft.getInstance().getWindow().handle()),
                            glfw_api
                    )
            ) {
                LOGGER.info(LOGGERMARKER, "Successfully set glfw context");
            } else {
                String error = "Failed to set glfw context";
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

            LOGGER.info(LOGGERMARKER, "Successfully initialized");
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public static void safe_init() {}

    public static boolean set_assume_context_current(boolean assume_context_current) throws Throwable {
        return (boolean) Abi.set_assume_context_current.invokeExact(assume_context_current ? 1 : 0);
    }

    public static boolean set_auto_paint(boolean auto_paint) throws Throwable {
        return (boolean) Abi.set_auto_paint.invokeExact(auto_paint ? 1 : 0);
    }

    public static boolean needs_tick() throws Throwable {
        return (boolean) Abi.needs_tick.invokeExact();
    }

    public static int tick() throws Throwable {
        return (int) Abi.tick.invokeExact();
    }
}

