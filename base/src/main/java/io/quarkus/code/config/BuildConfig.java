package io.quarkus.code.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "io.quarkus.code.build")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface BuildConfig {

    /**
     * This flag is used for releases that are just made for testing on staging and shouldn't reach production
     */
    @WithDefault("false")
    boolean stage();
}
