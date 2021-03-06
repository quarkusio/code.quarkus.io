package io.quarkus.code.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.quarkus.code.misc.QuarkusExtensionUtils
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.registry.catalog.ExtensionCatalog
import java.util.stream.Collectors

@JsonIgnoreProperties(ignoreUnknown = true)
class PlatformInfo(val key: String, val codeQuarkusExtensions: List<CodeQuarkusExtension>, val extensionCatalog: ExtensionCatalog) {

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
            .map { (this.extensionsByShortId[it] ?: throw IllegalArgumentException("Invalid shortId: $it")).id }
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
        if(this.extensionsById.containsKey(id)) {
            return this.extensionsById[id]!!.id
        }
        val found = this.extensionsById.entries
            .filter { QuarkusExtensionUtils.toShortcut(it.key) == QuarkusExtensionUtils.toShortcut(id) }
        if (found.size == 1) {
            return found[0].value.id
        }
        throw IllegalArgumentException("Invalid extension: $id")
    }
}