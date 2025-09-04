package io.quarkus.code.service;

import io.quarkus.code.model.CodeQuarkusExtension;

import java.util.function.Function;

public interface PlatformOverride {

    PlatformOverride DEFAULT_PLATFORM_OVERRIDE = new PlatformOverride.DefaultPlatformOverride();

    Function<CodeQuarkusExtension, CodeQuarkusExtension> extensionMapper();

    class DefaultPlatformOverride implements PlatformOverride {

        @Override
        public Function<CodeQuarkusExtension, CodeQuarkusExtension> extensionMapper() {
            return Function.identity();
        }
    }

}
