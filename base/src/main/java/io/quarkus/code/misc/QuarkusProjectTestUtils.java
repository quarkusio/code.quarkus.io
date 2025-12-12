package io.quarkus.code.misc;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QuarkusProjectTestUtils {

    private static final List<String> EXECUTABLES = List.of(
            "gradlew",
            "gradlew.bat",
            "mvnw",
            "mvnw.bat");

    private QuarkusProjectTestUtils() {
    }

    public static List<String> readFiles(File testDir) throws IOException {
        List<String> fileList = new ArrayList<>();
        Files.walk(testDir.toPath())
                .forEach(path -> {
                    String relativePath = testDir.toPath().relativize(path).toString();
                    fileList.add(relativePath + (path.toFile().isDirectory() ? "/" : ""));
                });
        return fileList;
    }

    public static String[] prefixFileList(String[] array, String prefix) {
        return Arrays.stream(array)
                .map(file -> prefix + file)
                .toArray(String[]::new);
    }

    public static Map.Entry<File, List<String>> extractProject(byte[] proj) throws IOException, ArchiveException {
        File testDir = Files.createTempDirectory("test-zip").toFile();
        System.out.println(testDir);
        File zipFile = new File(testDir, "project.zip");
        try (FileOutputStream output = new FileOutputStream(zipFile)) {
            output.write(proj);
        }
        List<String> zipList = unzip(testDir, zipFile);
        return new SimpleImmutableEntry<>(testDir, zipList);
    }

    public static List<String> unzip(File outputDir, File zipFile) throws IOException, ArchiveException {
        try (ArchiveInputStream<ZipArchiveEntry> ais = new ArchiveStreamFactory()
                .createArchiveInputStream(ArchiveStreamFactory.ZIP, Files.newInputStream(zipFile.toPath()));
                ZipArchiveInputStream zip = (ZipArchiveInputStream) ais) {
            List<String> fileList = new ArrayList<>();
            ZipArchiveEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                fileList.add(entry.getName());
                File file = new File(outputDir, entry.getName());

                // Check for Zip Slip vulnerability
                if (!file.toPath().normalize().startsWith(outputDir.toPath().normalize())) {
                    throw new IOException("Detected Zip Slip vulnerability: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    try (FileOutputStream output = new FileOutputStream(file)) {
                        zip.transferTo(output);
                    }
                    if (EXECUTABLES.contains(file.getName())) {
                        file.setExecutable(true);
                    }
                }
            }
            return fileList;
        }
    }
}
