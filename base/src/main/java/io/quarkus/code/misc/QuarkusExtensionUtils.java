package io.quarkus.code.misc;

import io.quarkus.code.model.CodeQuarkusExtension;
import io.quarkus.maven.dependency.ArtifactCoords;
import io.quarkus.platform.catalog.processor.CatalogProcessor;
import io.quarkus.platform.catalog.processor.ExtensionProcessor;
import io.quarkus.platform.catalog.processor.ProcessedCategory;
import io.quarkus.registry.catalog.Category;
import io.quarkus.registry.catalog.Extension;
import io.quarkus.registry.catalog.ExtensionCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QuarkusExtensionUtils {

    private static final List<String> TAG_KEYS = List.of("status", ".+-support", "with");

    public static String toShortcut(String id) {
        return id.replaceFirst("^(?:[^:]+:)?(?:quarkus-)?", "");
    }

    public static List<CodeQuarkusExtension> processExtensions(ExtensionCatalog catalog) {
        List<CodeQuarkusExtension> list = new ArrayList<>();
        List<ProcessedCategory> processedCategories = CatalogProcessor.getProcessedCategoriesInOrder(catalog);
        AtomicInteger order = new AtomicInteger();
        processedCategories.forEach(c -> {
            c.getSortedExtensions().forEach(e -> {
                CodeQuarkusExtension codeQExt = toCodeQuarkusExtension(e, c.getCategory(), order);
                if (codeQExt != null) {
                    list.add(codeQExt);
                }
            });
        });
        return list;
    }

    public static CodeQuarkusExtension toCodeQuarkusExtension(
            Extension ext,
            Category cat,
            AtomicInteger order) {
        if (ext == null || ext.getName() == null) {
            return null;
        }
        ExtensionProcessor extensionProcessor = ExtensionProcessor.of(ext);
        if (extensionProcessor.isUnlisted()) {
            return null;
        }
        String id = ext.managementKey();
        final ArtifactCoords bom = getBom(ext);
        return CodeQuarkusExtension.builder()
                .id(id)
                .shortId("ignored")
                .version(ext.getArtifact().getVersion())
                .name(ext.getName())
                .description(ext.getDescription())
                .shortName(extensionProcessor.getShortName())
                .category(cat.getName())
                .tags(getTags(extensionProcessor))
                .keywords(extensionProcessor.getExtendedKeywords())
                .transitiveExtensions(ExtensionProcessor.getMetadataValue(ext, "extension-dependencies").asStringList())
                .order(order.getAndIncrement())
                .providesCode(extensionProcessor.providesCode())
                .providesExampleCode(extensionProcessor.providesCode())
                .guide(extensionProcessor.getGuide())
                .platform(ext.hasPlatformOrigin())
                .bom("%s:%s:%s".formatted(bom.getGroupId(), bom.getArtifactId(), bom.getVersion()))
                .build();
    }

    private static List<String> getTags(ExtensionProcessor extension) {
        List<String> tags = new ArrayList<>();
        var data = extension.getSyntheticMetadata();
        for (var entry : data.entrySet()) {
            if (TAG_KEYS.stream().anyMatch(it -> entry.getKey().matches(it)) && !entry.getValue().isEmpty()) {
                tags.add(entry.getKey() + ":" + entry.getValue().iterator().next());
            }
        }
        return tags;
    }

    private static ArtifactCoords getBom(Extension extension) {
        if (extension.getOrigins() == null || extension.getOrigins().isEmpty()) {
            return null;
        } else {
            return extension.getOrigins().get(0).getBom();
        }
    }
}