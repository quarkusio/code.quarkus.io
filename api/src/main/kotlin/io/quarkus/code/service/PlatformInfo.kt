package io.quarkus.code.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.quarkus.code.misc.QuarkusExtensionUtils
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.registry.catalog.ExtensionCatalog
import java.util.logging.Logger
import java.util.stream.Collectors

@JsonIgnoreProperties(ignoreUnknown = true)
class PlatformInfo(
    val platformKey: String,
    val streamKey: String,
    val quarkusCoreVersion: String,
    val recommended: Boolean,
    val codeQuarkusExtensions: List<CodeQuarkusExtension>,
    val extensionCatalog: ExtensionCatalog,
) {

    val extensionsByShortId: Map<String, CodeQuarkusExtension>
        get() {
            return codeQuarkusExtensions.associateBy { it.shortId }
        }

    val extensionsById: Map<String, CodeQuarkusExtension>
        get() {
            return codeQuarkusExtensions.associateBy { it.id }
        }

    fun checkAndMergeExtensions(extensionsIds: Set<String>?, rawShortExtensions: String? = null): Set<String> {
        val fromId = (extensionsIds ?: setOf())
            .stream()
            .filter { it.isNotBlank() }
            .map { findById(it) }
            .collect(Collectors.toSet())
        val fromShortId = parseShortExtensions(rawShortExtensions).stream()
            .map { withVersionIfNeeded(this.extensionsByShortId[it] ?: throw IllegalArgumentException("Invalid shortId: $it")) }
            .collect(Collectors.toSet())
        return fromId union fromShortId
    }

    private fun parseShortExtensions(shortExtension: String?): Set<String> {
        return if (shortExtension.isNullOrBlank()) {
            setOf()
        } else {
            shortExtension.split(".").filter { it.isNotBlank() }.toSet()
        }
    }

    private fun findById(id: String): String {
        if (this.extensionsById.containsKey(id)) {
            return withVersionIfNeeded(this.extensionsById[id]!!)
        }
        val found = this.extensionsById
            .filterKeys { QuarkusExtensionUtils.toShortcut(it) == QuarkusExtensionUtils.toShortcut(id) }
        if (found.size == 1) {
            val ext = found.values.elementAt(0)
            return withVersionIfNeeded(ext)
        } else if(found.size > 1) {
            val core = found.filterKeys { it.startsWith("io.quarkus") }
            if (core.size == 1) {
                return withVersionIfNeeded(core.values.elementAt(0))
            }
        }
        throw IllegalArgumentException("Invalid extension: $id")
    }

    private fun withVersionIfNeeded(ext: CodeQuarkusExtension): String {
        if (!ext.platform) {
            return ext.id + ":${ext.version}"
        }
        return ext.id
    }

    companion object {
        private val LOG = Logger.getLogger(PlatformInfo::class.java.name)
    }
}