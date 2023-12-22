package io.quarkus.code.config;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "io.quarkus.code.github")
public interface GitHubConfig {

    Optional<String> clientId();

    Optional<String> clientSecret();
}