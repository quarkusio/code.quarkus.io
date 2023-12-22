package io.quarkus.code.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Optional;

@ConfigMapping(prefix = "io.quarkus.code.quarkus-platforms")
public interface PlatformConfig {

    @WithName("reload-cron-expr")
    String getReloadCronExpr();

    @WithName("registry-id")
    Optional<String> getRegistryId();
}
