package io.quarkus.code.services

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.dependencies.Category
import io.quarkus.dependencies.Extension
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.math.pow

object QuarkusExtensionUtils {

    private const val hashAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    private const val hashCharEncodeLength = hashAlphabet.length
    private const val hashMaxLength = 3
    private val maxHashCode: Int

    init {
        var max = 0
        for (i in 1..hashMaxLength) {
            max += hashCharEncodeLength.toDouble().pow(i).toInt()
        }
        maxHashCode = max
    }

    /**
     * This function will shorten the given string with the defined hashAlphabet and with a defined maximum length
     * Collisions are a possibility since only maxHashCode combination are available
     */
    fun shorten(input: String): String {
        var res = abs(input.hashCode()) % maxHashCode
        var hash = ""
        do {
            hash = hashAlphabet[res % hashCharEncodeLength] + hash
            res /= hashCharEncodeLength
        } while (res > 0)
        return hash
    }

    @JvmStatic
    fun processExtensions(descriptor: QuarkusPlatformDescriptor, config: ExtensionProcessorConfig): List<CodeQuarkusExtension> {
        val list = Lists.newArrayList<CodeQuarkusExtension>()

        val extById = descriptor.extensions.groupBy { toId(it) }
        val extByCategory = getExtByCategory(descriptor)
        val order = AtomicInteger()
        descriptor.categories.forEach { cat ->
            val pinnedList = getCategoryPinnedList(cat)
            val pinnedSet = pinnedList.toSet()
            pinnedList.forEach { id ->
                val codeQExt = toCodeQuarkusExtension(extById[id]?.get(0), cat, order, config)
                codeQExt?.let { list.add(it) }
            }
            extByCategory[cat.id]?.sortedBy { e -> e.name }?.forEach { ext ->
                if (!pinnedSet.contains(toId(ext))) {
                    val codeQExt = toCodeQuarkusExtension(ext, cat, order, config)
                    codeQExt?.let { list.add(it) }
                }
            }
        }
        return list
    }

    private fun getCategoryPinnedList(cat: Category): List<String> {
        if (cat.metadata?.get(Category.MD_PINNED) == null || cat.metadata[Category.MD_PINNED] !is List<*>) {
            return emptyList()
        }
        @Suppress("UNCHECKED_CAST")
        return cat.metadata["pinned"] as List<String>
    }


    @JvmStatic
    fun toCodeQuarkusExtension(ext: Extension?, cat: Category, order: AtomicInteger, config: ExtensionProcessorConfig): CodeQuarkusExtension? {
        if (ext == null || ext.name == null) {
            return null
        }
        if (isExtensionUnlisted(ext)) {
            return null
        }
        val keywords = ext.keywords ?: emptyList()
        val id = "${ext.groupId}:${ext.artifactId}"
        val shortId = createShortId(id)
        return CodeQuarkusExtension(
                id = id,
                shortId = shortId,
                version = ext.version,
                name = ext.name,
                description = ext.description,
                shortName = getExtensionShortName(ext),
                category = cat.name,
                status = getExtensionStatus(ext),
                tags = getExtensionTags(ext, config.tagsFrom),
                default = ext.artifactId == "quarkus-resteasy",
                keywords = keywords,
                order = order.getAndIncrement(),
                labels = keywords,
                guide = getExtensionGuide(ext)
        )
    }

    internal fun createShortId(id: String): String {
        val normalized = id.replace("^(io.quarkus:)?quarkus-".toRegex(), "")
        return shorten(normalized)
    }

    private fun getExtensionStatus(ext: Extension) =
            ext.metadata?.get(Extension.MD_STATUS) as String? ?: "stable"

    private fun getExtensionTags(ext: Extension, tagsFrom: List<String>): List<String> {
        val b = ImmutableList.builder<String>()
        return tagsFrom.map {
            normalizeToList(ext.metadata.get(it))
        }.flatten()
    }

    private fun normalizeToList(value: Any?): List<String> {
        if (value is String) {
            return listOf(value)
        } else if (value is List<*>) {
            @Suppress("UNCHECKED_CAST")
            return value as List<String>
        }
        return listOf()
    }

    private fun getExtensionGuide(ext: Extension) =
            ext.metadata?.get(Extension.MD_GUIDE) as String?

    private fun getExtensionShortName(ext: Extension) =
            ext.metadata?.get(Extension.MD_SHORT_NAME) as String?

    private fun isExtensionUnlisted(ext: Extension): Boolean {
        val unlisted = ext.metadata?.get(Extension.MD_UNLISTED)
        if (unlisted !== null) {
            if (unlisted is Boolean) {
                return unlisted
            } else if (unlisted is String) {
                return unlisted.toBoolean()
            }
        }
        return false
    }

    @JvmStatic
    fun toId(e: Extension) = "${e.groupId}:${e.artifactId}"

    @JvmStatic
    fun getExtByCategory(descriptor: QuarkusPlatformDescriptor): Map<String, List<Extension>> {
        val extByCategory = HashMap<String, ArrayList<Extension>>()
        descriptor.extensions.forEach {
            val categories = it.metadata?.get("categories")
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