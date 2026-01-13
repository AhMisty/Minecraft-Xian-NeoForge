package cn.ahmisty.minecraft.xian.ffi;

import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public final class Library {
    public static final Linker LINKER = Linker.nativeLinker();
    public static Path BASE = FMLPaths.GAMEDIR.get().resolve("xian").resolve("libs");
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("Library");
    private static final Logger LOGGER = LoggerFactory.getLogger("仙");

    public final Arena ARENA;
    public final Path PATH;
    public final Path DIR;
    public final Path NAME;
    public SymbolLookup INSTANCE = name -> Optional.empty();

    public Library(String name, Arena arena) throws Throwable {
        ARENA = arena;
        PATH = BASE.resolve(System.mapLibraryName(name));
        DIR = PATH.getParent();
        NAME = PATH.getFileName();
        final String pathInJar = "xian/libs/" + NAME.toString();
        if (!Files.exists(PATH)) {
            LOGGER.warn(LOGGERMARKER, "Failed to find the native library {} at {}, try to find in jar path {}", NAME, DIR, pathInJar);
            try (InputStream stream = Library.class.getClassLoader().getResourceAsStream(pathInJar)) {
                if (stream == null) {
                    throw new UnsatisfiedLinkError("Failed to find the native library " + NAME + " at " + PATH + " or in jar path " + pathInJar);
                }
                Files.createDirectories(DIR);
                Files.copy(stream, PATH, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException error) {
                throw new UnsatisfiedLinkError("Failed to extract the native library " + NAME + " to " + PATH).initCause(error);
            }
        }

        try {
            INSTANCE = SymbolLookup.libraryLookup(PATH, ARENA);
            LOGGER.info(LOGGERMARKER, "Successfully loaded the native library {}", NAME);
        } catch (Throwable error) {
            throw new UnsatisfiedLinkError("Failed to load the native library " + NAME + " from " + PATH).initCause(error);
        }
    }

    public Optional<MemorySegment> find(String name) throws UnsatisfiedLinkError {
        Optional<MemorySegment> symbol = INSTANCE.find(name);
        if (symbol.isEmpty()) {
            throw new UnsatisfiedLinkError("Failed to load the symbol " + name + " in library " + NAME);
        }
        return symbol;
    }

    public MethodHandle loadFunctionCritical(String name, FunctionDescriptor descriptor) {
        return find(name)
                .map(segment -> LINKER.downcallHandle(segment, descriptor, Linker.Option.critical(false)))
                .orElse(null);
    }

    public MethodHandle loadFunction(String name, FunctionDescriptor descriptor) {
        return find(name)
                .map(segment -> LINKER.downcallHandle(segment, descriptor))
                .orElse(null);
    }

    public MethodHandle loadFunction(String name, FunctionDescriptor descriptor, Linker.Option... options) {
        return find(name)
                .map(segment -> LINKER.downcallHandle(segment, descriptor, options))
                .orElse(null);
    }
}
