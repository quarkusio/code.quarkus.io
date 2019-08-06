package io.launcher.quarkus.model

data class QuarkusProject(
    val groupId: String = DEFAULT_GROUPID,
    val artifactId: String = DEFAULT_ARTIFACTID,
    val version: String = DEFAULT_VERSION,
    val className: String = DEFAULT_CLASSNAME,
    val path: String = DEFAULT_PATH,
    val extensions: Set<String> = setOf()
) {
    companion object {
        const val DEFAULT_GROUPID = "org.acme"
        const val DEFAULT_ARTIFACTID= "code-with-quarkus"
        const val DEFAULT_VERSION = "1.0.0-SNAPSHOT"
        const val DEFAULT_CLASSNAME = "org.acme.ExampleResource"
        const val DEFAULT_PATH = "/hello"
    }
}