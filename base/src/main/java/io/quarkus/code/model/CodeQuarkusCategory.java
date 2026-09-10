package io.quarkus.code.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CodeQuarkusCategory(
        String id,
        String name) {
}
