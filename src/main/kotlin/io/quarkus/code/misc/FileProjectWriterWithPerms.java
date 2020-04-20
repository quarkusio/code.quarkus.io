package io.quarkus.code.misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * ProjectWriter implementation to create direct files in the file system.
 */
public class FileProjectWriterWithPerms extends io.quarkus.cli.commands.writer.FileProjectWriter implements ProjectWriterWithPerms {

    private final File root;

    public FileProjectWriterWithPerms(File file) {
        super(file);
        root = file;
    }

    public void write(String path, byte[] contentBytes, boolean allowExec) throws IOException {
        final Path outputPath = root.toPath().resolve(path);
        Files.write(outputPath, contentBytes);
        if(allowExec) {
            Files.setPosixFilePermissions(outputPath, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
    }
}
