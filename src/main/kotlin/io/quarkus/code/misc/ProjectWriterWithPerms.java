package io.quarkus.code.misc;

import java.io.IOException;

public interface ProjectWriterWithPerms {
    void write(String path, byte[] contentBytes, boolean allowExec) throws IOException;
}
