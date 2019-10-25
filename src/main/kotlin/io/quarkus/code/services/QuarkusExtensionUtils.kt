package io.quarkus.code.services

import com.google.common.collect.Lists
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.dependencies.Category
import io.quarkus.dependencies.Extension
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor
import java.util.concurrent.atomic.AtomicInteger

object QuarkusExtensionUtils {

    @JvmStatic
    fun processExtensions(descriptor: QuarkusPlatformDescriptor): List<CodeQuarkusExtension> {
        val list = Lists.newArrayList<CodeQuarkusExtension>()

        val extById = descriptor.extensions.groupBy { toId(it) }
        val extByCategory = getExtByCategory(descriptor)
        val order = AtomicInteger()
        descriptor.categories.forEach { cat ->
            val pinnedList = if (cat.metadata != null && cat.metadata[Category.MD_PINNED] != null && cat.metadata[Category.MD_PINNED] is List<*>) cat.metadata["pinned"] as List<String> else emptyList()
            val pinnedSet = pinnedList.toSet()
            pinnedList.forEach { id ->
                val codeQExt = toCodeQuarkusExtension(extById[id]?.get(0), cat, order)
                codeQExt?.let { list.add(it) }

            }
            extByCategory[cat.id]?.sortedBy { e -> e.name }?.forEach { ext ->
                if (!pinnedSet.contains(toId(ext))) {
                    val codeQExt = toCodeQuarkusExtension(ext, cat, order)
                    codeQExt?.let { list.add(it) }
                }
            }
        }
        return list
    }

    @JvmStatic
    fun toCodeQuarkusExtension(ext: Extension?, cat: Category, order: AtomicInteger): CodeQuarkusExtension? {
        if (ext == null) {
            return null
        }
        val unlisted = ext.metadata[Extension.MD_UNLISTED] !== null && ext.metadata[Extension.MD_UNLISTED] as Boolean
        if (unlisted) {
            return null
        }
        return CodeQuarkusExtension(
                "${ext.groupId}:${ext.artifactId}",
                ext.name,
                ext.description,
                ext.metadata[Extension.MD_SHORT_NAME] as String?,
                cat.name,
                ext.metadata[Extension.MD_STATUS] as String? ?: "stable",
                ext.artifactId == "quarkus-resteasy",
                ext.keywords,
                order.getAndIncrement(),
                ext.keywords
        )

    }

    @JvmStatic
    fun toId(e: Extension) = "${e.groupId}:${e.artifactId}"

    @JvmStatic
    fun getExtByCategory(descriptor: QuarkusPlatformDescriptor): Map<String, List<Extension>> {
        val extByCategory = HashMap<String, ArrayList<Extension>>()
        descriptor.extensions.forEach {
            val categories = it.metadata["categories"]
            if (categories is Collection<*>) {
                categories.forEach { cat ->
                    if (cat is String) {
                        if (extByCategory[cat] == null) {
                            extByCategory[cat] = ArrayList()
                        }
                        extByCategory[cat]?.add(it)
                    }
                }
            }
        }
        return extByCategory
    }
}