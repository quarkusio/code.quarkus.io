package io.quarkus.code.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern
import javax.ws.rs.DefaultValue
import javax.ws.rs.QueryParam

@JsonIgnoreProperties(ignoreUnknown = true)
class ProjectDefinition {

    companion object {
        const val DEFAULT_GROUPID = "org.acme"
        const val DEFAULT_ARTIFACTID = "code-with-quarkus"
        const val DEFAULT_VERSION = "1.0.0-SNAPSHOT"
        const val DEFAULT_BUILDTOOL = "MAVEN"
        const val DEFAULT_NO_CODE = false

        const val GROUPID_PATTERN = "^([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*\$"
        const val ARTIFACTID_PATTERN = "^[a-z][a-z0-9-._]*\$"
        const val CLASSNAME_PATTERN = GROUPID_PATTERN
        const val PATH_PATTERN = "^\\/([a-z0-9\\-._~%!\$&'()*+,;=:@]+\\/?)*\$"
        const val BUILDTOOL_PATTERN = "^(MAVEN)|(GRADLE)|(GRADLE_KOTLIN_DSL)\$"
    }

    constructor()
    constructor(streamKey: String? = null,
                groupId: String = DEFAULT_GROUPID,
                artifactId: String = DEFAULT_ARTIFACTID,
                version: String = DEFAULT_VERSION,
                className: String? = null,
                path: String? = null,
                buildTool: String = DEFAULT_BUILDTOOL,
                noExamples: Boolean = false,
                noCode: Boolean = false,
                extensions: Set<String> = setOf(),
                shortExtensions: String = "") {
        this.streamKey = streamKey
        this.groupId = groupId
        this.artifactId = artifactId
        this.version = version
        this.className = className
        this.extensions = extensions
        this.path = path
        this.buildTool = buildTool
        this.noCode = noCode
        this.noExamples = noExamples
        this.shortExtensions = shortExtensions
    }

    @QueryParam("S")
    @Parameter(name = "S", description = "The platform stream to use to create this project ('platformKey:streamId' or 'streamId')", required = false)
    var streamKey: String? = null
        private set

    @DefaultValue(DEFAULT_GROUPID)
    @NotEmpty
    @Pattern(regexp = GROUPID_PATTERN)
    @QueryParam("g")
    @Parameter(name = "g", description = "GAV: groupId", required = false)
    var groupId: String = DEFAULT_GROUPID
        private set

    @DefaultValue(DEFAULT_ARTIFACTID)
    @NotEmpty
    @Pattern(regexp = ARTIFACTID_PATTERN)
    @QueryParam("a")
    @Parameter(name = "a", description = "GAV: artifactId", required = false)
    var artifactId: String = DEFAULT_ARTIFACTID
        private set

    @DefaultValue(DEFAULT_VERSION)
    @NotEmpty
    @QueryParam("v")
    @Parameter(name = "v", description = "GAV: version", required = false)
    var version: String = DEFAULT_VERSION
        private set

    @QueryParam("c")
    @Pattern(regexp = CLASSNAME_PATTERN)
    @Parameter(name = "c", description = "The class name to use in the generated application", required = false)
    var className: String? = null
        private set

    @QueryParam("p")
    @Pattern(regexp = PATH_PATTERN)
    @Parameter(name = "p", description = "The path of the REST endpoint created in the generated application", required = false)
    var path: String? = null
        private set

    @DefaultValue(DEFAULT_NO_CODE.toString())
    @QueryParam("ne")
    @Parameter(name = "ne", description = "No code examples (Deprecated: use noCode (nc) instead)", required = false)
    @Deprecated(message = "Use noCode (nc) instead")
    var noExamples: Boolean = DEFAULT_NO_CODE
        private set

    @DefaultValue(DEFAULT_NO_CODE.toString())
    @QueryParam("nc")
    @Parameter(name = "nc", description = "No code", required = false)
        var noCode: Boolean = DEFAULT_NO_CODE
        private set

    @DefaultValue(DEFAULT_BUILDTOOL)
    @NotEmpty
    @QueryParam("b")
    @Pattern(regexp = BUILDTOOL_PATTERN)
    @Parameter(name = "b", description = "The build tool to use (MAVEN or GRADLE)", required = false)
    var buildTool: String = DEFAULT_BUILDTOOL
        private set

    @QueryParam("e")
    @Parameter(name = "e", description = "The set of extension ids that will be included in the generated application", required = false)
    var extensions: Set<String> = setOf()
        private set

    @QueryParam("s")
    @DefaultValue("")
    @Parameter(name = "s", description = "The set of extension shortIds separated by a '.' that will be included in the generated application (Deprecated: see https://github.com/quarkusio/code.quarkus.io/issues/424)", required = false)
    @Deprecated(message = "see https://github.com/quarkusio/code.quarkus.io/issues/424")
    var shortExtensions: String = ""
        private set

    override fun toString(): String {
        return "QuarkusProject(streamKey='$streamKey', groupId='$groupId', artifactId='$artifactId', version='$version', className='$className', path='$path', buildTool='$buildTool', noCode='$noCode', extensions=$extensions')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectDefinition

        if (streamKey != other.streamKey) return false
        if (groupId != other.groupId) return false
        if (artifactId != other.artifactId) return false
        if (version != other.version) return false
        if (className != other.className) return false
        if (path != other.path) return false
        if (noExamples != other.noExamples) return false
        if (noCode != other.noCode) return false
        if (buildTool != other.buildTool) return false
        if (extensions != other.extensions) return false
        if (shortExtensions != other.shortExtensions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = streamKey?.hashCode() ?: 0
        result = 31 * result + groupId.hashCode()
        result = 31 * result + artifactId.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + (className?.hashCode() ?: 0)
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + noExamples.hashCode()
        result = 31 * result + noCode.hashCode()
        result = 31 * result + buildTool.hashCode()
        result = 31 * result + extensions.hashCode()
        result = 31 * result + shortExtensions.hashCode()
        return result
    }
}