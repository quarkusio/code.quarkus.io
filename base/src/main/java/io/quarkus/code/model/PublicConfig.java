package io.quarkus.code.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public record PublicConfig(
        String environment,
        String segmentWriteKey,
        String sentryDSN,
        String quarkusPlatformVersion,
        String quarkusDevtoolsVersion,
        @Deprecated String quarkusVersion,
        String gitHubClientId,
        String gitCommitId) {
}