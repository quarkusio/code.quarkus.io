package io.quarkus.code.config;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "io.quarkus.code")
public interface CodeQuarkusConfig {

    String id();

    String name();

    Optional<String> quarkusPlatformVersion();

    Optional<String> quarkusDevtoolsVersion();

    Optional<String> gitCommitId();

    Optional<String> environment();

    Optional<String> sentryFrontendDSN();

    Optional<String> hostname();
}
