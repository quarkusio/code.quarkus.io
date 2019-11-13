package io.quarkus.code.quarkus.model

data class PublicConfig(
        val environment: String,
        val gaTrackingId: String,
        val sentryDSN: String,
        val quarkusVersion: String,
        val gitCommitId: String?
)