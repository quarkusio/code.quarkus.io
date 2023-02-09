package io.quarkus.code.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern
import javax.ws.rs.DefaultValue
import javax.ws.rs.QueryParam

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectDefinition(val streamKey: String? = null,
                             val groupId: String = DEFAULT_GROUPID,
                             val artifactId: String = DEFAULT_ARTIFACTID,
                             val version: String = DEFAULT_VERSION,
                             val className: String? = null,
                             val path: String? = null,
                             val buildTool: String = DEFAULT_BUILDTOOL,
                             val javaVersion: String = DEFAULT_JAVA_VERSION,
                             val noCode: Boolean = false,
                             val extensions: Set<String> = setOf()) {

    companion object {
        const val DEFAULT_GROUPID = "org.acme"
        const val DEFAULT_ARTIFACTID = "code-with-quarkus"
        const val DEFAULT_VERSION = "1.0.0-SNAPSHOT"
        const val DEFAULT_BUILDTOOL = "MAVEN"
        const val DEFAULT_NO_CODE = false
        const val DEFAULT_JAVA_VERSION = "17"

        const val JAVA_VERSION_PATTERN = "^(?:1\\.)?(\\d+)(?:\\..*)?\$";
        const val GROUPID_PATTERN = "^([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*\$"
        const val ARTIFACTID_PATTERN = "^[a-z][a-z0-9-._]*\$"
        const val CLASSNAME_PATTERN = GROUPID_PATTERN
        const val PATH_PATTERN = "^\\/([a-z0-9\\-._~%!\$&'()*+,;=:@]+\\/?)*\$"
        const val BUILDTOOL_PATTERN = "^(MAVEN)|(GRADLE)|(GRADLE_KOTLIN_DSL)\$"
    }
}