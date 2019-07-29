package io.launcher.quarkus.model

import javax.validation.constraints.NotBlank
import javax.ws.rs.DefaultValue
import javax.ws.rs.QueryParam


class QuarkusProject {
    @NotBlank
    @QueryParam("g")
    @DefaultValue("org.example")
    var groupId: String = "org.example"

    @NotBlank
    @QueryParam("a")
    @DefaultValue("quarkus-app")
    var artifactId: String = "quarkus-app"

    @NotBlank
    @QueryParam("v")
    @DefaultValue("0.0.1-SNAPSHOT")
    var version: String = "0.0.1-SNAPSHOT"

    @NotBlank
    @QueryParam("c")
    @DefaultValue("org.example.ExampleResource")
    var className: String = "org.example.ExampleResource"

    @NotBlank
    @QueryParam("p")
    @DefaultValue("/hello")
    var path: String = "/hello"

    @QueryParam("e")
    var extensions: Set<String> = mutableSetOf()
}