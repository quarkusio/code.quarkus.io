package io.quarkus.code.model

data class Stream(
    val key: String,
    val quarkusCoreVersion: String,
    val recommended: Boolean,
    val status: StreamStatus
)

enum class StreamStatus {
    FINAL, CR
}
