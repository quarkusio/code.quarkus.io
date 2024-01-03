package io.quarkus.code.model;

import java.util.SortedSet;

public record Stream(
        String key,
        String quarkusCoreVersion,
        JavaCompatibility javaCompatibility,
        String platformVersion,
        boolean recommended,
        String status,
        boolean lts) {
    public static record JavaCompatibility(SortedSet<Integer> versions, int recommended) {
    }

    public static StreamBuilder builder() {
        return new StreamBuilder();
    }

    public static class StreamBuilder {
        private String key;
        private String quarkusCoreVersion;
        private JavaCompatibility javaCompatibility;
        private String platformVersion;
        private boolean recommended;
        private String status;
        private boolean lts;

        private StreamBuilder() {
        }

        public StreamBuilder key(String key) {
            this.key = key;
            return this;
        }

        public StreamBuilder quarkusCoreVersion(String quarkusCoreVersion) {
            this.quarkusCoreVersion = quarkusCoreVersion;
            return this;
        }

        public StreamBuilder javaCompatibility(JavaCompatibility javaCompatibility) {
            this.javaCompatibility = javaCompatibility;
            return this;
        }

        public StreamBuilder platformVersion(String platformVersion) {
            this.platformVersion = platformVersion;
            return this;
        }

        public StreamBuilder recommended(boolean recommended) {
            this.recommended = recommended;
            return this;
        }

        public StreamBuilder status(String status) {
            this.status = status;
            return this;
        }

        public StreamBuilder lts(boolean lts) {
            this.lts = lts;
            return this;
        }

        public Stream build() {
            return new Stream(key, quarkusCoreVersion, javaCompatibility, platformVersion, recommended, status, lts);
        }

    }
}