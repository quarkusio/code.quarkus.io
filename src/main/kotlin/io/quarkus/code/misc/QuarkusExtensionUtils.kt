package io.quarkus.code.misc

import com.google.common.collect.Lists
import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.registry.catalog.Category
import io.quarkus.registry.catalog.Extension
import io.quarkus.registry.catalog.ExtensionCatalog
import java.util.Locale
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.pow

object QuarkusExtensionUtils {
    private const val MD_PINNED = "pinned"
    private const val hashAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    private const val hashCharEncodeLength = hashAlphabet.length
    private const val hashMaxLength = 3
    private val maxHashCode: Int
    private val stopWords = setOf("the", "and", "you", "that", "was", "for", "are", "with", "his", "they", "one",
            "have", "this", "from", "had", "not", "but", "what", "can", "out", "other", "were", "all", "there", "when",
            "your", "how", "each", "she", "which", "their", "will", "way", "about", "many", "then", "them", "would", "enable",
            "these", "her", "him", "has", "over", "than", "who", "may", "down", "been", "more", "implementing", "non", "quarkus")
    private val tokenizerPattern = Pattern.compile("\\w+");

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

    fun toShortcut(id: String): String = id.replace(Regex("^([^:]+:)?(quarkus-)?"), "")

    @JvmStatic
    fun processExtensions(catalog: ExtensionCatalog, config: ExtensionProcessorConfig): List<CodeQuarkusExtension> {
        val list = Lists.newArrayList<CodeQuarkusExtension>()

        val extById = catalog.extensions.groupBy { toId(it) }
        val extByCategory = getExtByCategory(catalog)
        val order = AtomicInteger()
        catalog.categories.forEach { cat ->
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
        if (cat.metadata?.get(MD_PINNED) == null || cat.metadata[MD_PINNED] !is List<*>) {
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
        val id = toId(ext)
        val shortId = createShortId(id)
        return CodeQuarkusExtension(
                id = id,
                shortId = shortId,
                version = ext.artifact.version,
                name = ext.name,
                description = ext.description,
                shortName = getExtensionShortName(ext),
                category = cat.name,
                default = false,
                status = getExtensionStatus(ext),
                tags = getExtensionTags(ext, listOf(config.tagsFrom.orElse("status"))),
                keywords = toKeywords(ext.keywords ?: emptyList(), ext.description),
                order = order.getAndIncrement(),
                providesExampleCode = providesExampleCode(ext),
                labels = ext.keywords ?: emptyList(),
                guide = getExtensionGuide(ext)
        )
    }

    fun toKeywords(keywords: List<String>, description: String): List<String> {
        val result = TreeSet<String>()
        keywords.forEach { result.add(it.toLowerCase(Locale.US)) }
        val matcher = tokenizerPattern.matcher(description)
        while (matcher.find()) {
            val token = matcher.group().toLowerCase(Locale.US)
            if (token.length >= 3 && !stopWords.contains(token)) {
                result.add(token)
            }
        }
        return ArrayList<String>(result);
    }

    internal fun createShortId(id: String): String {
        val normalized = id.replace("^(io.quarkus:)?quarkus-".toRegex(), "")
        return shorten(normalized)
    }

    private fun getExtensionStatus(ext: Extension): String {
        val list = normalizeToList(ext.metadata?.get(Extension.MD_STATUS))
        return if (list.isEmpty()) "stable" else list[0]
    }

    private fun getExtensionTags(ext: Extension, tagsFrom: List<String>): List<String> {
        val tags = tagsFrom.map {
            normalizeToList(ext.metadata[it])
        }
                .flatten()
                .filter { it != "stable" }
                .map { it.toLowerCase() }
        if (providesExampleCode(ext)) {
            return tags.plus("provides-example")
        }
        return tags
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

    private fun providesExampleCode(ext: Extension): Boolean =
            !(ext.metadata?.get(Extension.MD_CODESTART) as String?).isNullOrBlank()
                    && "quarkus-kotlin" != ext.artifact.artifactId
                    && "quarkus-scala" != ext.artifact.artifactId

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
    fun toId(e: Extension) = "${e.artifact.groupId}:${e.artifact.artifactId}"

    @JvmStatic
    fun getExtByCategory(catalog: ExtensionCatalog): Map<String, List<Extension>> {
        val extByCategory = HashMap<String, ArrayList<Extension>>()
        catalog.extensions.forEach {
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