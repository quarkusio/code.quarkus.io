package io.quarkus.code.gen

import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions
import io.quarkus.code.misc.QuarkusExtensionUtils.shorten
import io.quarkus.code.misc.QuarkusExtensionUtils.toShortcut
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.registry.catalog.ExtensionCatalog
import io.quarkus.registry.catalog.json.JsonCatalogMapperHelper
import io.quarkus.registry.catalog.json.JsonExtensionCatalog
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.junit.jupiter.api.Test
import java.util.*

internal class QuarkusExtensionUtilsTest {
    companion object {
        const val FAKE_CATALOG_JSON = "fakeextensions.json"
    }

    val config = object : ExtensionProcessorConfig {
        override val tagsFrom: Optional<String> = Optional.empty()
    }

    @Test
    internal fun testShorten() {
        assertThat(shorten("some random long string"), `is`("ODO"))
        assertThat(shorten(""), `is`("a"))
        assertThat(shorten("some-id"), `is`("gLa"))
        assertThat(shorten("io.quarkus:quarkus-arc"), `is`("3eJ"))
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

        val extensions = processExtensions(catalog = getTestCatalog(), config = config)
        assertThat(extensions[0], `is`(CodeQuarkusExtension(
                id = "io.quarkus:quarkus-resteasy",
                shortId = "98e",
                version = "999-SNAPSHOT",
                name = "RESTEasy JAX-RS",
                description = "REST endpoint framework implementing JAX-RS and more",
                shortName = "jax-rs",
                category = "Web",
                tags = listOf("provides-code"),
                providesCode = true,
                providesExampleCode = true,
                keywords = listOf("endpoint", "framework", "jax", "jaxrs", "quarkus-resteasy", "rest", "resteasy", "web"),
                guide = "https://quarkus.io/guides/rest-json",
                order = 0))
        )
        assertThat(extensions[5], `is`(CodeQuarkusExtension(
                id = "io.quarkus:quarkus-rest-client-mutiny",
                shortId = "Ph0",
                version = "999-SNAPSHOT",
                name = "Mutiny support for REST Client",
                description = "Enable Mutiny for the REST client",
                shortName = "Mutiny support for REST Client",
                category = "Web",
                providesCode = false,
                providesExampleCode = false,
                tags = listOf("preview"),
                keywords = listOf("client", "microprofile-rest-client", "mutiny", "quarkus-rest-client-mutiny", "rest", "rest-client", "rest-client-mutiny", "web-client"),
                guide = null,
                order = 5))
        )
    }

    @Test
    internal fun testOrder() {
        val extensions = processExtensions(getTestCatalog(), config)
        assertThat(extensions.map { it.name }.subList(0, 5), contains(
                "RESTEasy JAX-RS",
                "RESTEasy Jackson",
                "RESTEasy JSON-B",
                "Eclipse Vert.x GraphQL",
                "Hibernate Validator"))
        assertThat(extensions.map { it.name }.subList(extensions.size - 5, extensions.size), contains(
                "Quarkus Extension for Spring Scheduled",
                "Quarkus Extension for Spring Security API",
                "Quarkus Extension for Spring Web API",
                "Kotlin",
                "Scala"))
    }


    private fun getTestCatalog(): ExtensionCatalog {
        val inputString = javaClass.classLoader.getResourceAsStream(FAKE_CATALOG_JSON)
            ?: throw IllegalStateException("Failed to locate $FAKE_CATALOG_JSON on the classpath")
        val catalog = JsonCatalogMapperHelper.deserialize(inputString, JsonExtensionCatalog::class.java)
        return catalog
    }
}