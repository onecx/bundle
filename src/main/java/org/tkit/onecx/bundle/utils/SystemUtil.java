package org.tkit.onecx.bundle.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class SystemUtil {

    public static boolean createDirectory(String path) throws Exception {
        return createDirectory(Paths.get(path));
    }

    public static boolean createDirectory(Path path) throws Exception {
        if (Files.exists(path)) {
            return false;
        }
        return path.toFile().mkdirs();
    }

    public static void deleteDirectory(String path) throws Exception {
        deleteDirectory(Paths.get(path));
    }

    public static void deleteDirectory(Path path) throws Exception {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        Files.deleteIfExists(path);
    }
}
