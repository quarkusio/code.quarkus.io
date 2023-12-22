package io.quarkus.code.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import io.quarkus.code.misc.QuarkusExtensionUtils;
import io.quarkus.code.model.CodeQuarkusExtension;
import io.quarkus.code.model.ExtensionRef;
import io.quarkus.code.model.Stream;
import io.quarkus.registry.catalog.ExtensionCatalog;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformInfo {
    private final String platformKey;
    private final Stream stream;
    private final String quarkusCoreVersion;
    private final String platformVersion;
    private final boolean recommended;
    private final List<CodeQuarkusExtension> codeQuarkusExtensions;
    private final ExtensionCatalog extensionCatalog;

    private final Map<String, ExtensionRef> extensionsById;

    public PlatformInfo(String platformKey, Stream stream, String quarkusCoreVersion, String platformVersion,
                        boolean recommended, List<CodeQuarkusExtension> codeQuarkusExtensions,
                        ExtensionCatalog extensionCatalog) {
        this.platformKey = platformKey;
        this.stream = stream;
        this.quarkusCoreVersion = quarkusCoreVersion;
        this.platformVersion = platformVersion;
        this.recommended = recommended;
        this.codeQuarkusExtensions = codeQuarkusExtensions;
        this.extensionCatalog = extensionCatalog;
        this.extensionsById = codeQuarkusExtensions.stream()
                .collect(Collectors.toMap(CodeQuarkusExtension::id, CodeQuarkusExtension::toExtensionRef, (a, b) -> {
                    if (Objects.equals(a, b)) {
                        return a;
                    }
                    throw new IllegalStateException("Duplicate key " + a);
                }));
    }

    public List<CodeQuarkusExtension> codeQuarkusExtensions() {
        return codeQuarkusExtensions;
    }

    public String platformKey() {
        return platformKey;
    }

    public Stream stream() {
        return stream;
    }

    public String quarkusCoreVersion() {
        return quarkusCoreVersion;
    }

    public String platformVersion() {
        return platformVersion;
    }

    public boolean recommended() {
        return recommended;
    }


    public ExtensionCatalog extensionCatalog() {
        return extensionCatalog;
    }

    public Map<String, ExtensionRef> extensionsById() {
        return extensionsById;
    }

    public Set<String> checkAndMergeExtensions(Set<String> extensionsIds) {
        return extensionsIds.stream()
                .filter(not(Strings::isNullOrEmpty))
                .map(this::findById)
                .collect(Collectors.toSet());
    }

    private String findById(String id) {
        if (this.extensionsById.containsKey(id)) {
            return withVersionIfNeeded(this.extensionsById.get(id));
        }
        var found = this.extensionsById.entrySet().stream()
                .filter(entry -> QuarkusExtensionUtils.toShortcut(entry.getKey()).equals(QuarkusExtensionUtils.toShortcut(id)))
                .map(Map.Entry::getValue)
                .toList();
        if (found.size() == 1) {
            var ext = found.get(0);
            return withVersionIfNeeded(ext);
        } else if (found.size() > 1) {
            var core = found.stream()
                    .filter(ext -> ext.id().startsWith("io.quarkus"))
                    .toList();
            if (core.size() == 1) {
                var ext = core.get(0);
                return withVersionIfNeeded(ext);
            }
        }
        throw new IllegalArgumentException("Invalid extension: " + id);
    }

    private String withVersionIfNeeded(ExtensionRef ext) {
        if (!ext.platform()) {
            return ext.id() + ":" + ext.version();
        }
        return ext.id();
    }

}