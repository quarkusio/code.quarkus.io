package io.quarkus.code.misc

import com.google.common.collect.Lists
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.maven.dependency.ArtifactCoords
import io.quarkus.platform.catalog.processor.CatalogProcessor.getProcessedCategoriesInOrder
import io.quarkus.platform.catalog.processor.ExtensionProcessor
import io.quarkus.registry.catalog.Category
import io.quarkus.registry.catalog.Extension
import io.quarkus.registry.catalog.ExtensionCatalog
import java.util.concurrent.atomic.AtomicInteger

object QuarkusExtensionUtils {

    val TAG_KEYS = listOf("status", ".+-support", "with")

    fun toShortcut(id: String): String = id.replace(Regex("^([^:]+:)?(quarkus-)?"), "")

    @JvmStatic
    fun processExtensions(catalog: ExtensionCatalog): List<CodeQuarkusExtension> {
        val list = Lists.newArrayList<CodeQuarkusExtension>()
        val processedCategories = getProcessedCategoriesInOrder(catalog)
        val order = AtomicInteger()
        processedCategories.forEach { c ->
            c.sortedExtensions.forEach { e ->
                val codeQExt = toCodeQuarkusExtension(e, c.category, order)
                codeQExt?.let { list.add(it) }
            }
        }
        return list
    }

    @JvmStatic
    fun toCodeQuarkusExtension(
        ext: Extension?,
        cat: Category,
        order: AtomicInteger
    ): CodeQuarkusExtension? {
        if (ext == null || ext.name == null) {
            return null
        }
        val extensionProcessor = ExtensionProcessor.of(ext)
        if (extensionProcessor.isUnlisted) {
            return null
        }
        val id = ext.managementKey()
        return CodeQuarkusExtension(
            id = id,
            shortId = "ignored",
            version = ext.artifact.version,
            name = ext.name,
            description = ext.description,
            shortName = extensionProcessor.shortName,
            category = cat.name,
            tags = getTags(extensionProcessor),
            keywords = extensionProcessor.extendedKeywords,
            order = order.getAndIncrement(),
            providesExampleCode = extensionProcessor.providesCode(),
            providesCode = extensionProcessor.providesCode(),
            guide = extensionProcessor.guide,
            platform = ext.hasPlatformOrigin(),
            bom = getBom(ext)?.let { "${it.groupId}:${it.artifactId}:${it.version}" }
        )
    }

    private fun getTags(extension: ExtensionProcessor): List<String> {
        val tags = arrayListOf<String>()
        val data = extension.syntheticMetadata
        for (entry in data.entries) {
            if (TAG_KEYS.any { Regex(it).matches(entry.key) } && !entry.value.isEmpty()) {
                tags.add(entry.key + ":" + entry.value.first())
            }
        }
        return tags;
    }

    private fun getBom(extension: Extension): ArtifactCoords? {
        return if (extension.origins == null || extension.origins.isEmpty()) {
            null
        } else extension.origins[0].bom
    }
}