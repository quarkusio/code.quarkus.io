package io.quarkus.code.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public record PublicConfig(
        String environment,
        String segmentWriteKey,
        String sentryDSN,
        String quarkusPlatformVersion,
        String quarkusDevtoolsVersion,
        @Deprecated String quarkusVersion,
        String gitHubClientId,
        String gitCommitId,
        boolean stageBuild) {

    public static PublicConfig.Builder builder() {
        return new PublicConfig.Builder();
    }

    public static class Builder {
        private String environment;
        private String segmentWriteKey;
        private String sentryDSN;
        private String quarkusPlatformVersion;
        private String quarkusDevtoolsVersion;
        private String quarkusVersion;
        private String gitHubClientId;
        private String gitCommitId;
        private boolean stageBuild = false;

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder segmentWriteKey(String segmentWriteKey) {
            this.segmentWriteKey = segmentWriteKey;
            return this;
        }

        public Builder sentryDSN(String sentryDSN) {
            this.sentryDSN = sentryDSN;
            return this;
        }

        public Builder quarkusPlatformVersion(String quarkusPlatformVersion) {
            this.quarkusPlatformVersion = quarkusPlatformVersion;
            return this;
        }

        public Builder quarkusDevtoolsVersion(String quarkusDevtoolsVersion) {
            this.quarkusDevtoolsVersion = quarkusDevtoolsVersion;
            return this;
        }

        public Builder quarkusVersion(String quarkusVersion) {
            this.quarkusVersion = quarkusVersion;
            return this;
        }

        public Builder gitHubClientId(String gitHubClientId) {
            this.gitHubClientId = gitHubClientId;
            return this;
        }

        public Builder gitCommitId(String gitCommitId) {
            this.gitCommitId = gitCommitId;
            return this;
        }

        public Builder stageBuild(boolean stageBuild) {
            this.stageBuild = stageBuild;
            return this;
        }

        public PublicConfig build() {
            return new PublicConfig(
                    environment,
                    segmentWriteKey,
                    sentryDSN,
                    quarkusPlatformVersion,
                    quarkusDevtoolsVersion,
                    quarkusVersion,
                    gitHubClientId,
                    gitCommitId,
                    stageBuild);
        }
    }
}