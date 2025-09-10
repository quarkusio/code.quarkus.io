package io.quarkus.code.config;

import java.util.Optional;

public interface UIConfig {

    String id();

    String name();

    Optional<String> favicon();

    Optional<String> apiUrl();

    Optional<String> publicUrl();
}
