package io.launcher.quarkus.model

data class Config(
    val environment: String,
    val gaTrackingId: String?,
    val sentryDSN: String?
)