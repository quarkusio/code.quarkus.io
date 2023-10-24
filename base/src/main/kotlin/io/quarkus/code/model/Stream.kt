package io.quarkus.code.model

import java.util.SortedSet

data class Stream(
    val key: String,
    val quarkusCoreVersion: String,
    val javaCompatibility: JavaCompatibility,
    val platformVersion: String,
    val recommended: Boolean,
    val status: String,
    val lts: Boolean
) {
    data class JavaCompatibility(val versions: SortedSet<Int>, val recommended: Int)
}
