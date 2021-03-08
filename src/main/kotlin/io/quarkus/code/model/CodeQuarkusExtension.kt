package io.quarkus.code.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.*

@JsonInclude(NON_NULL)
data class CodeQuarkusExtension(
        val id: String,

        @Deprecated(message = "see https://github.com/quarkusio/code.quarkus.io/issues/424")
        val shortId: String,

        val version: String,
        val name: String,
        val description: String?,
        val shortName: String?,
        val category: String,
        val tags: List<String>,
        val keywords: List<String>,
        val providesExampleCode: Boolean,
        val guide: String?,
        val order: Int,

        @Deprecated(message = "no continued")
        val default: Boolean,

        @Deprecated(message = "has been replaced", replaceWith = ReplaceWith("tags"))
        val status: String,

        @Deprecated(message = "has been replaced", replaceWith = ReplaceWith("keywords"))
        val labels: List<String>
)