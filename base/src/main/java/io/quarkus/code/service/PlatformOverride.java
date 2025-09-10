package io.quarkus.code.service;

import io.quarkus.code.model.CodeQuarkusExtension;
import io.quarkus.code.model.Preset;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public interface PlatformOverride {

    PlatformOverride DEFAULT_PLATFORM_OVERRIDE = new PlatformOverride.DefaultPlatformOverride();

    Function<CodeQuarkusExtension, CodeQuarkusExtension> extensionMapper();

    List<Preset> presets();

    List<String> extensionTagsMapper(List<String> tags);

    class DefaultPlatformOverride implements PlatformOverride {

        private static final Set<String> TAGS = Set.of(
                "with:starter-code", "status:stable", "status:preview", "status:experimental", "status:deprecated");

        @Override
        public Function<CodeQuarkusExtension, CodeQuarkusExtension> extensionMapper() {
            return Function.identity();
        }

        @Override
        public List<Preset> presets() {
            return PlatformService.DEFAULT_PRESETS;
        }

        @Override
        public List<String> extensionTagsMapper(List<String> tags) {
            return tags.stream().filter(TAGS::contains).toList();
        }
    }

}
