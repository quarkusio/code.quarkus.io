package io.launcher.quarkus.model

class Config(
    var environment: String,
    var gaTrackingId: String?,
    var sentryDSN: String?
)