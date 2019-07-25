package io.launcher.quarkus.model

import javax.validation.constraints.NotBlank
import javax.ws.rs.DefaultValue
import javax.ws.rs.QueryParam


class QuarkusProject {
    @NotBlank
    @QueryParam("g")
    @DefaultValue("org.example")
    var groupId: String = ""

    @NotBlank
    @QueryParam("a")
    @DefaultValue("quarkus-app")
    var artifactId: String = "p"

    @NotBlank
    @QueryParam("v")
    @DefaultValue("0.0.1-SNAPSHOT")
    var version: String = ""

    @NotBlank
    @QueryParam("c")
    @DefaultValue("org.example.QuarkusApp")
    var className: String = ""

    @QueryParam("e")
    var dependencies: Set<String> = mutableSetOf()
}