package io.quarkus.code.model;

public record IntegratedDependency(
        String name,
        String artifact,
        String version) {

}
