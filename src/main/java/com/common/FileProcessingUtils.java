package com.common;

import static java.nio.file.StandardOpenOption.CREATE;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileProcessingUtils {

    public static String loadFile(String className, String resourceFile) {
        String basePath = className.replace('.', '/');
        URL fileUrl = Thread.currentThread().getContextClassLoader().getResource(String.format("data/%s/%s", basePath, resourceFile));

        if (fileUrl == null) {
            return null;
        }

        return loadFile(new File(fileUrl.getFile()));
    }

    public static String loadFile(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return String.join("\n", lines);
    }


    public static void saveFile(String className, String resourceFile, String sql) {
        String basePath = className.replace('.', File.separatorChar);
        Path fileUrl = Paths.get("src", "test", "resources",
                String.format("data" + File.separator + "%s" + File.separator + "%s", basePath,
                        resourceFile));
        try {
            if (Files.exists(fileUrl)) {
                Files.delete(fileUrl);
            }
            Files.write(fileUrl, sql.getBytes(StandardCharsets.UTF_8), CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
