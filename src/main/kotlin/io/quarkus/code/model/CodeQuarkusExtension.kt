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
        @Deprecated(message = "use providesCode instead")
        val providesExampleCode: Boolean,

        val providesCode: Boolean,
        val guide: String?,
        val order: Int,
        val platform: Boolean,
        val bom: String?

)