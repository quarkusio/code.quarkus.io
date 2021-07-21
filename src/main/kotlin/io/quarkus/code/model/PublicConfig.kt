package io.quarkus.code.model

data class PublicConfig(
    val environment: String,
    val gaTrackingId: String?,
    val sentryDSN: String?,
    val quarkusPlatformVersion: String,
    val quarkusDevtoolsVersion: String,
    val gitHubClientId: String?,
    val features: List<String>,
    val gitCommitId: String?
)