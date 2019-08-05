package io.launcher.quarkus.model

class Config(
    val environment: String,
    val gaTrackingId: String?,
    val sentryDSN: String?
)