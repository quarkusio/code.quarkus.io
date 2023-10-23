package io.quarkus.code.model

data class Stream(
    val key: String,
    val quarkusCoreVersion: String,
    val platformVersion: String,
    val recommended: Boolean,
    val status: String,
    val lts: Boolean
)
