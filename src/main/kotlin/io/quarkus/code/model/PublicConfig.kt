package io.quarkus.code.model

data class PublicConfig(
        val environment: String,
        val gaTrackingId: String?,
        val sentryDSN: String?,
        val quarkusVersion: String,
        val gitCommitId: String?,
        val features: List<String>
)