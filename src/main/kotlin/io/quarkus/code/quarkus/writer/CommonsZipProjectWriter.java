package io.quarkus.code.quarkus.writer;

import io.quarkus.cli.commands.writer.ProjectWriter;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipExtraField;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * ProjectWriter implementation to create a zip.
 */
public class CommonsZipProjectWriter implements ProjectWriter {

    // Remove 1 day to be sure the date won't be in the past in any location
    private static final Date CREATION = new Date(System.currentTimeMillis() - 24 * 3600000);

    private final ArchiveOutputStream aos;
    private final String basePath;
    private final Set<String> createdDirs = new HashSet<>();
    private final Map<String, ZipArchiveEntry> archiveEntriesByPath = new LinkedHashMap<>();
    private final Map<String, byte[]> contentByPath = new LinkedHashMap<>();

    private CommonsZipProjectWriter(final ArchiveOutputStream aos, final String basePath) {
        this.aos = aos;
        this.basePath = basePath;
    }

    public static CommonsZipProjectWriter createWriter(final OutputStream outputStream, final String basePath) throws ArchiveException {
        final ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, outputStream);
        return new CommonsZipProjectWriter(aos, basePath);
    }

    @Override
    public String mkdirs(String path) {
        if (path.length() == 0) {
            return "";
        }
        return Paths.get(path).toString();
    }

    @Override
    public void write(String path, String content) throws IOException {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        this.write(path, contentBytes, false);
    }

    private void mkdirs(Path dirPath) throws IOException {
        if (dirPath == null) {
            return;
        }
        mkdirs(dirPath.getParent());
        String dirPathText = dirPath.toString();
        if (!createdDirs.contains(dirPathText)) {
            ZipArchiveEntry ze = new ZipArchiveEntry(dirPathText + "/");
            ze.setUnixMode(040755);
            ze.setExtraFields(new ZipExtraField[]{getTimestamp()});
            aos.putArchiveEntry(ze);
            aos.closeArchiveEntry();
            createdDirs.add(dirPathText);
        }
    }


    public void write(String path, byte[] contentBytes, boolean allowExec) {
        Path filePath = Paths.get(this.basePath, "/", path);
        ZipArchiveEntry ze = new ZipArchiveEntry(filePath.toString());
        if (allowExec) {
            ze.setUnixMode(0100755);
        } else {
            ze.setUnixMode(0100644);
        }
        ze.setExtraFields(new ZipExtraField[]{getTimestamp()});
        archiveEntriesByPath.put(filePath.toString(), ze);
        contentByPath.put(filePath.toString(), contentBytes);
    }

    @Override
    public byte[] getContent(String path) {
        return contentByPath.get(Paths.get(this.basePath, "/", path).toString());
    }

    @Override
    public boolean exists(String path) {
        return contentByPath.containsKey(Paths.get(this.basePath, "/", path).toString());
    }

    @Override
    public File getProjectFolder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasFile() {
        return false;
    }

    @Override
    public void close() throws IOException {
        for (Map.Entry<String, byte[]> entry : contentByPath.entrySet()) {
            ZipArchiveEntry ze = archiveEntriesByPath.get(entry.getKey());
            this.mkdirs(Paths.get(entry.getKey()).getParent());
            aos.putArchiveEntry(ze);
            aos.write(entry.getValue());
            aos.closeArchiveEntry();
        }
        aos.close();
    }

    private X5455_ExtendedTimestamp getTimestamp() {
        X5455_ExtendedTimestamp timestamp = new X5455_ExtendedTimestamp();
        timestamp.setCreateJavaTime(CREATION);
        timestamp.setModifyJavaTime(CREATION);
        timestamp.setAccessJavaTime(CREATION);
        return timestamp;
    }

}
