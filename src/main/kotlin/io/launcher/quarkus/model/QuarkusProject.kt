package io.launcher.quarkus.model

import javax.validation.constraints.NotBlank
import javax.ws.rs.DefaultValue
import javax.ws.rs.QueryParam


class QuarkusProject {
    @NotBlank
    @QueryParam("g")
    @DefaultValue("org.acme")
    var groupId: String = "org.acme"

    @NotBlank
    @QueryParam("a")
    @DefaultValue("code-with-quarkus")
    var artifactId: String = "code-with-quarkus"

    @NotBlank
    @QueryParam("v")
    @DefaultValue("1.0.0-SNAPSHOT")
    var version: String = "1.0.0-SNAPSHOT"

    @NotBlank
    @QueryParam("c")
    @DefaultValue("org.acme.ExampleResource")
    var className: String = "org.acme.ExampleResource"

    @NotBlank
    @QueryParam("p")
    @DefaultValue("/hello")
    var path: String = "/hello"

    @QueryParam("e")
    var extensions: Set<String> = mutableSetOf()
}