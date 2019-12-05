package io.quarkus.code.model

data class Config(
        val environment: String,
        val gaTrackingId: String?,
        val sentryDSN: String?,
        val quarkusVersion: String,
        val gitCommitId: String?
)