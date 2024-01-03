package io.quarkus.code.model;

public record ExtensionRef(
        String id,
        String version,
        boolean platform) {

}
