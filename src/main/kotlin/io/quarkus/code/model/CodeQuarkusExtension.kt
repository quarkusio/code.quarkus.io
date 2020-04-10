package io.quarkus.code.model

data class CodeQuarkusExtension(
        val id: String,
        val shortId: String,
        val version: String,
        val name: String,
        val description: String?,
        val shortName: String?,
        val category: String,
        val tags: List<String>,
        val default: Boolean,
        val keywords: List<String>,
        val guide: String?,
        val order: Int,

        @Deprecated(message = "has been replaced", replaceWith = ReplaceWith("tags"))
        val status: String,

        @Deprecated(message = "has been replaced", replaceWith = ReplaceWith("keywords"))
        val labels: List<String>
)