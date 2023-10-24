package io.quarkus.code.misc

import io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions
import io.quarkus.code.misc.QuarkusExtensionUtils.toShortcut
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.registry.catalog.ExtensionCatalog
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.junit.jupiter.api.Test

internal class QuarkusExtensionUtilsTest {
    companion object {
        const val FAKE_CATALOG_JSON = "fakeextensions.json"
    }

    @Test
    internal fun testToShortcut() {
        assertThat(toShortcut("io.quarkus:quarkus-my-ext"), `is`("my-ext"))
        assertThat(toShortcut("org.apache.camel.quarkus:camel-quarkus-core"), `is`("camel-quarkus-core"))
        assertThat(toShortcut("camel-quarkus-core"), `is`("camel-quarkus-core"))
        assertThat(toShortcut("quarkus-my-ext"), `is`("my-ext"))
        assertThat(toShortcut("io.quarkiverse.myext:quarkus-my-ext"), `is`("my-ext"))
    }

    @Test
    internal fun textContent() {

        val extensions = processExtensions(catalog = getTestCatalog())
        assertThat(
            extensions[0], `is`(
                CodeQuarkusExtension(
                    id = "io.quarkus:quarkus-resteasy",
                    shortId = "ignored",
                    version = "999-SNAPSHOT",
                    name = "RESTEasy JAX-RS",
                    description = "REST endpoint framework implementing JAX-RS and more",
                    shortName = "jax-rs",
                    category = "Web",
                    tags = listOf("with:starter-code", "status:stable"),
                    keywords = setOf("endpoint", "framework", "jax", "jaxrs", "jax-rs", "quarkus-resteasy", "rest", "resteasy", "web"),
                    providesExampleCode = true,
                    providesCode = true,
                    guide = "https://quarkus.io/guides/rest-json",
                    order = 0,
                    platform = true,
                    bom = "io.quarkus:quarkus-bom:999-SNAPSHOT",
                )
            )
        )
        assertThat(
            extensions[6], `is`(
                CodeQuarkusExtension(
                    id = "io.quarkus:quarkus-rest-client-mutiny",
                    shortId = "ignored",
                    version = "999-SNAPSHOT",
                    name = "Mutiny support for REST Client",
                    description = "Enable Mutiny for the REST client",
                    category = "Web",
                    tags = listOf("status:preview"),
                    keywords = setOf(
                        "rest",
                        "reactive",
                        "web",
                        "web-client",
                        "rest-client",
                        "client",
                        "quarkus-rest-client-mutiny",
                        "microprofile-rest-client",
                        "support",
                        "mutiny",
                        "rest-client-mutiny"
                    ),
                    providesExampleCode = false,
                    providesCode = false,
                    guide = null,
                    order = 6,
                    platform = true,
                    bom = "io.quarkus:quarkus-bom:999-SNAPSHOT"
                )
            )
        )
    }

    @Test
    internal fun testOrder() {
        val extensions = processExtensions(getTestCatalog())
        assertThat(
            extensions.map { it.name }.subList(0, 5), contains(
                "RESTEasy JAX-RS",
                "RESTEasy Jackson",
                "RESTEasy JSON-B",
                "Eclipse Vert.x GraphQL",
                "gRPC"
            )
        )
    }

    private fun getTestCatalog(): ExtensionCatalog {
        val inputString = javaClass.classLoader.getResourceAsStream(FAKE_CATALOG_JSON)
            ?: throw IllegalStateException("Failed to locate $FAKE_CATALOG_JSON on the classpath")
        return ExtensionCatalog.fromStream(inputString)
    }
}