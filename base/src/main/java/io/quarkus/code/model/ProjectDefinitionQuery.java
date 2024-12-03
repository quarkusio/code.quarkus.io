package io.quarkus.code.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import java.util.Set;

import static io.quarkus.code.model.ProjectDefinition.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDefinitionQuery {

    @QueryParam("S")
    @Parameter(name = "S", description = "The platform stream to use to create this project ('platformKey:streamId' or 'streamId')", required = false)
    @Schema(description = "The platform stream to use to create this project ('platformKey:streamId' or 'streamId')", required = false)
    String streamKey;

    @DefaultValue(DEFAULT_GROUPID)
    @Pattern(regexp = GROUPID_PATTERN)
    @QueryParam("g")
    @Parameter(name = "g", description = "GAV: groupId", required = false)
    @Schema(description = "GAV: groupId", required = false, defaultValue = DEFAULT_GROUPID, pattern = GROUPID_PATTERN)
    String groupId = DEFAULT_GROUPID;

    @DefaultValue(DEFAULT_ARTIFACTID)
    @Pattern(regexp = ARTIFACTID_PATTERN)
    @QueryParam("a")
    @Parameter(name = "a", description = "GAV: artifactId", required = false)
    @Schema(description = "GAV: artifactId", required = false, defaultValue = DEFAULT_ARTIFACTID, pattern = ARTIFACTID_PATTERN)
    String artifactId = DEFAULT_ARTIFACTID;

    @DefaultValue(DEFAULT_VERSION)
    @QueryParam("v")
    @Parameter(name = "v", description = "GAV: version", required = false)
    @Schema(description = "GAV: version", required = false, defaultValue = DEFAULT_VERSION)
    String version = DEFAULT_VERSION;

    @QueryParam("c")
    @Pattern(regexp = CLASSNAME_PATTERN)
    @Parameter(name = "c", description = "The class name to use in the generated application", required = false)
    @Schema(description = "The class name to use in the generated application", required = false, pattern = CLASSNAME_PATTERN)
    String className;

    @QueryParam("p")
    @Pattern(regexp = PATH_PATTERN)
    @Parameter(name = "p", description = "The path of the REST endpoint created in the generated application", required = false)
    @Schema(description = "The path of the REST endpoint created in the generated application", required = false, pattern = PATH_PATTERN)
    String path;

    /**
     * @deprecated use noCode (nc) instead
     */
    @DefaultValue(DEFAULT_NO_CODE_STRING)
    @QueryParam("ne")
    @Parameter(name = "ne", description = "No code examples (Deprecated: use noCode (nc) instead)", required = false, schema = @Schema(deprecated = true))
    @Schema(description = "No code examples (Deprecated: use noCode (nc) instead)", deprecated = true, required = false, defaultValue = DEFAULT_NO_CODE_STRING)
    @Deprecated
    boolean noExamples = DEFAULT_NO_CODE;

    @DefaultValue(DEFAULT_NO_CODE_STRING)
    @QueryParam("nc")
    @Parameter(name = "nc", description = "No code", required = false)
    @Schema(description = "No code", required = false, defaultValue = DEFAULT_NO_CODE_STRING)
    boolean noCode = DEFAULT_NO_CODE;

    @DefaultValue(DEFAULT_BUILDTOOL)
    @QueryParam("b")
    @Pattern(regexp = BUILDTOOL_PATTERN)
    @Parameter(name = "b", description = "The build tool to use (MAVEN, GRADLE or GRADLE_KOTLIN_DSL)", required = false, schema = @Schema(enumeration = {
            "MAVEN", "GRADLE", "GRADLE_KOTLIN_DSL" }))
    @Schema(description = "The build tool to use (MAVEN, GRADLE or GRADLE_KOTLIN_DSL)", enumeration = { "MAVEN", "GRADLE",
            "GRADLE_KOTLIN_DSL" }, defaultValue = DEFAULT_BUILDTOOL)
    String buildTool = DEFAULT_BUILDTOOL;

    @QueryParam("j")
    @Parameter(name = "j", description = "The Java version for the generation application", required = false)
    @Schema(description = "The Java version for the generation application", required = false)
    Integer javaVersion;

    @QueryParam("e")
    @Parameter(name = "e", description = "The set of extension ids that will be included in the generated application", required = false)
    @Schema(description = "The set of extension ids that will be included in the generated application", required = false)
    Set<String> extensions = Set.of();

    public ProjectDefinition toProjectDefinition() {
        return new ProjectDefinition(
                streamKey,
                groupId,
                artifactId,
                version,
                className,
                path,
                buildTool,
                javaVersion,
                noCode,
                noExamples,
                extensions);
    }

}