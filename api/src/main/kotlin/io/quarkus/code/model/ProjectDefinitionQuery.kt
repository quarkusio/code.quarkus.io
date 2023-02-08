package io.quarkus.code.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.quarkus.code.model.ProjectDefinition.Companion.ARTIFACTID_PATTERN
import io.quarkus.code.model.ProjectDefinition.Companion.BUILDTOOL_PATTERN
import io.quarkus.code.model.ProjectDefinition.Companion.CLASSNAME_PATTERN
import io.quarkus.code.model.ProjectDefinition.Companion.DEFAULT_ARTIFACTID
import io.quarkus.code.model.ProjectDefinition.Companion.DEFAULT_BUILDTOOL
import io.quarkus.code.model.ProjectDefinition.Companion.DEFAULT_GROUPID
import io.quarkus.code.model.ProjectDefinition.Companion.DEFAULT_JAVA_VERSION
import io.quarkus.code.model.ProjectDefinition.Companion.DEFAULT_NO_CODE
import io.quarkus.code.model.ProjectDefinition.Companion.DEFAULT_VERSION
import io.quarkus.code.model.ProjectDefinition.Companion.GROUPID_PATTERN
import io.quarkus.code.model.ProjectDefinition.Companion.JAVA_VERSION_PATTERN
import io.quarkus.code.model.ProjectDefinition.Companion.PATH_PATTERN
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern
import javax.ws.rs.DefaultValue
import javax.ws.rs.QueryParam

@JsonIgnoreProperties(ignoreUnknown = true)
class ProjectDefinitionQuery {

    @QueryParam("S")
    @Parameter(name = "S", description = "The platform stream to use to create this project ('platformKey:streamId' or 'streamId')", required = false)
    @Schema(description = "The platform stream to use to create this project ('platformKey:streamId' or 'streamId')", required = false)
    var streamKey: String? = null
        private set

    @DefaultValue(DEFAULT_GROUPID)
    @NotEmpty
    @Pattern(regexp = GROUPID_PATTERN)
    @QueryParam("g")
    @Parameter(name = "g", description = "GAV: groupId", required = false)
    @Schema(description = "GAV: groupId", required = false, defaultValue = DEFAULT_GROUPID, pattern = GROUPID_PATTERN)
    var groupId: String = DEFAULT_GROUPID
        private set

    @DefaultValue(DEFAULT_ARTIFACTID)
    @NotEmpty
    @Pattern(regexp = ARTIFACTID_PATTERN)
    @QueryParam("a")
    @Parameter(name = "a", description = "GAV: artifactId", required = false)
    @Schema(description = "GAV: artifactId", required = false, defaultValue = DEFAULT_ARTIFACTID, pattern = ARTIFACTID_PATTERN)
    var artifactId: String = DEFAULT_ARTIFACTID
        private set

    @DefaultValue(DEFAULT_VERSION)
    @NotEmpty
    @QueryParam("v")
    @Parameter(name = "v", description = "GAV: version", required = false)
    @Schema(description = "GAV: version", required = false, defaultValue = DEFAULT_VERSION)
    var version: String = DEFAULT_VERSION
        private set

    @QueryParam("c")
    @Pattern(regexp = CLASSNAME_PATTERN)
    @Parameter(name = "c", description = "The class name to use in the generated application", required = false)
    @Schema(description = "The class name to use in the generated application", required = false, pattern = CLASSNAME_PATTERN)
    var className: String? = null
        private set

    @QueryParam("p")
    @Pattern(regexp = PATH_PATTERN)
    @Parameter(name = "p", description = "The path of the REST endpoint created in the generated application", required = false)
    @Schema(description = "The path of the REST endpoint created in the generated application", required = false, pattern = PATH_PATTERN)
    var path: String? = null
        private set

    @DefaultValue(DEFAULT_NO_CODE.toString())
    @QueryParam("ne")
    @Parameter(name = "ne", description = "No code examples (Deprecated: use noCode (nc) instead)", required = false, schema = Schema(deprecated = true))
    @Schema(description = "No code examples (Deprecated: use noCode (nc) instead)", deprecated = true, required = false, defaultValue = DEFAULT_NO_CODE.toString())
    @Deprecated(message = "Use noCode (nc) instead")
    var noExamples: Boolean = DEFAULT_NO_CODE
        private set

    @DefaultValue(DEFAULT_NO_CODE.toString())
    @QueryParam("nc")
    @Parameter(name = "nc", description = "No code", required = false)
    @Schema(description = "No code", required = false, defaultValue = DEFAULT_NO_CODE.toString())
        var noCode: Boolean = DEFAULT_NO_CODE
        private set

    @DefaultValue(DEFAULT_BUILDTOOL)
    @NotEmpty
    @QueryParam("b")
    @Pattern(regexp = BUILDTOOL_PATTERN)
    @Parameter(name = "b", description = "The build tool to use (MAVEN, GRADLE or GRADLE_KOTLIN_DSL)", required = false, schema = Schema(enumeration = ["MAVEN", "GRADLE", "GRADLE_KOTLIN_DSL"]))
    @Schema(description = "The build tool to use (MAVEN, GRADLE or GRADLE_KOTLIN_DSL)", enumeration = ["MAVEN", "GRADLE", "GRADLE_KOTLIN_DSL"], defaultValue = DEFAULT_BUILDTOOL)
    var buildTool: String = DEFAULT_BUILDTOOL
        private set

    @QueryParam("j")
    @NotEmpty
    @DefaultValue(DEFAULT_JAVA_VERSION)
    @Pattern(regexp = JAVA_VERSION_PATTERN)
    @Parameter(name = "j", description = "The Java version for the generation application", required = false)
    @Schema(description = "The Java version for the generation application", required = false, pattern = JAVA_VERSION_PATTERN)
    var javaVersion: String = DEFAULT_JAVA_VERSION
        private set


    @QueryParam("e")
    @Parameter(name = "e", description = "The set of extension ids that will be included in the generated application", required = false)
    @Schema(description = "The set of extension ids that will be included in the generated application", required = false)
    var extensions: Set<String> = setOf()
        private set

    fun toProjectDefinition(): ProjectDefinition {
        return ProjectDefinition(
            streamKey = streamKey,
            groupId = groupId,
            artifactId = artifactId,
            version = version,
            className = className,
            path = path,
            buildTool = buildTool,
            javaVersion = javaVersion,
            noCode = noCode || noExamples,
            extensions = extensions
        )
    }
}