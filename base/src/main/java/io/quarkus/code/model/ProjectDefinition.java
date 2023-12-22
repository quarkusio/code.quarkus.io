package io.quarkus.code.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;
import java.util.Set;

@JsonDeserialize(builder = ProjectDefinition.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProjectDefinition(
        String streamKey,

        @NotEmpty
        @Pattern(regexp = GROUPID_PATTERN)
        String groupId,

        @NotEmpty
        @Pattern(regexp = ARTIFACTID_PATTERN)
        String artifactId,

        @NotEmpty
        String version,

        @Pattern(regexp = CLASSNAME_PATTERN)
        String className,

        @Pattern(regexp = PATH_PATTERN)
        String path,

        @NotEmpty
        @Pattern(regexp = BUILDTOOL_PATTERN)
        String buildTool,
        Integer javaVersion,
        Boolean noCode,
        Boolean noExamples,
        Set<String> extensions
) {

    public ProjectDefinition {
        Objects.requireNonNull(groupId, "groupId is required");
        Objects.requireNonNull(artifactId, "artifactId is required");
        Objects.requireNonNull(version, "version is required");
        Objects.requireNonNull(buildTool, "buildTool is required");
        Objects.requireNonNull(noCode, "noCode is required");
        Objects.requireNonNull(noExamples, "noExamples is required");
        Objects.requireNonNull(extensions, "extensions is required");
    }

    public static final String DEFAULT_GROUPID = "org.acme";
    public static final String DEFAULT_ARTIFACTID = "code-with-quarkus";
    public static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";
    public static final String DEFAULT_BUILDTOOL = "MAVEN";
    public static final Boolean DEFAULT_NO_CODE = false;
    public static final String DEFAULT_NO_CODE_STRING = "false";

    public static final String GROUPID_PATTERN = "^([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*$";
    public static final String ARTIFACTID_PATTERN = "^[a-z][a-z0-9-._]*$";
    public static final String CLASSNAME_PATTERN = GROUPID_PATTERN;
    public static final String PATH_PATTERN = "^\\/([a-z0-9\\-._~%!$&'()*+,;=:@]+\\/?)*$";
    public static final String BUILDTOOL_PATTERN = "^(MAVEN)|(GRADLE)|(GRADLE_KOTLIN_DSL)$";


    public static ProjectDefinition of() {
       return new ProjectDefinition(null, DEFAULT_GROUPID, DEFAULT_ARTIFACTID, DEFAULT_VERSION, null, null, DEFAULT_BUILDTOOL, null, DEFAULT_NO_CODE, DEFAULT_NO_CODE, Set.of());
    }

    @JsonCreator
    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String streamKey = null;
        private String groupId = DEFAULT_GROUPID;
        private String artifactId = DEFAULT_ARTIFACTID;
        private String version = DEFAULT_VERSION;
        private String className = null;
        private String path = null;
        private String buildTool = DEFAULT_BUILDTOOL;
        private Integer javaVersion = null;
        private Boolean noCode = DEFAULT_NO_CODE;
        private Boolean noExamples = DEFAULT_NO_CODE;
        private Set<String> extensions = Set.of();

        private Builder() {
        }

        public Builder streamKey(String streamKey) {
            this.streamKey = streamKey;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder buildTool(String buildTool) {
            this.buildTool = buildTool;
            return this;
        }

        public Builder javaVersion(Integer javaVersion) {
            this.javaVersion = javaVersion;
            return this;
        }

        public Builder noCode(Boolean noCode) {
            this.noCode = noCode;
            return this;
        }

        public Builder noExamples(Boolean noExamples) {
            this.noExamples = noExamples;
            return this;
        }

        public Builder extensions(Set<String> extensions) {
            this.extensions = extensions;
            return this;
        }

        public ProjectDefinition build() {
            return new ProjectDefinition(streamKey, groupId, artifactId, version, className, path, buildTool, javaVersion, noCode, noExamples, extensions);
        }
    }

}