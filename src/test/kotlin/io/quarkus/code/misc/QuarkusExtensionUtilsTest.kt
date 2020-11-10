package io.quarkus.code.gen

import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions
import io.quarkus.code.misc.QuarkusExtensionUtils.shorten
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.platform.descriptor.loader.json.ArtifactResolver
import io.quarkus.platform.descriptor.loader.json.QuarkusJsonPlatformDescriptorLoaderContext
import io.quarkus.platform.descriptor.loader.json.impl.QuarkusJsonPlatformDescriptor
import io.quarkus.platform.descriptor.loader.json.impl.QuarkusJsonPlatformDescriptorLoaderImpl
import org.apache.maven.model.Dependency
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.util.*
import java.util.function.Function

internal class QuarkusExtensionUtilsTest {
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
    internal fun textContent() {

        val extensions = processExtensions(descriptor = getTestDescriptor(), config = config)
        assertThat(extensions[0], `is`(CodeQuarkusExtension(
                id = "io.quarkus:quarkus-arc",
                shortId = "zmg",
                version = "999-SNAPSHOT",
                name = "ArC",
                description = "Build time CDI dependency injection",
                shortName = "CDI",
                category = "Core",
                tags = listOf("test", "provides-example"),
                default = false,
                providesExampleCode = true,
                keywords = listOf("arc", "build", "cdi", "dependency", "dependency-injection", "di", "injection", "time"),
                guide = "https://quarkus.io/guides/cdi-reference",
                order = 0,
                status = "test",
                labels = listOf("arc", "cdi", "dependency-injection", "di")))
        )
        assertThat(extensions[5], `is`(CodeQuarkusExtension(
                id = "io.quarkus:quarkus-netty",
                shortId = "rpC",
                version = "999-SNAPSHOT",
                name = "Netty",
                description = "Netty is a non-blocking I/O client-server framework. Used by Quarkus as foundation layer.",
                shortName = null,
                category = "Web",
                providesExampleCode = false,
                tags = listOf(),
                default = false,
                keywords = listOf("blocking", "client", "foundation", "framework", "layer", "netty", "non", "quarkus", "server", "used"),
                guide = null,
                order = 5,
                status = "stable",
                labels = listOf()))
        )
    }

    @Test
    internal fun testOrder() {
        val extensions = processExtensions(getTestDescriptor(), config)
        assertThat(extensions.map { it.name }.subList(0, 5), contains(
                "ArC",
                "RESTEasy JAX-RS",
                "RESTEasy JSON-B",
                "RESTEasy Jackson",
                "Hibernate Validator"))
        assertThat(extensions.map { it.name }.subList(extensions.size - 5, extensions.size), contains(
                "Quarkus Extension for Spring DI API",
                "Quarkus Extension for Spring Data JPA API",
                "Quarkus Extension for Spring Web API",
                "Kotlin",
                "Scala"))
    }


    private fun getTestDescriptor(): QuarkusJsonPlatformDescriptor {
        val qpd = QuarkusJsonPlatformDescriptorLoaderImpl()

        val artifactResolver = object : ArtifactResolver {

            override fun <T> process(groupId: String, artifactId: String, classifier: String, type: String, version: String,
                                     processor: Function<Path, T>): T {
                throw UnsupportedOperationException()
            }
        }

        val context = object : QuarkusJsonPlatformDescriptorLoaderContext(artifactResolver) {
            override fun <T> parseJson(parser: Function<InputStream, T>): T {
                val resourceName = "fakeextensions.json"

                val `is` = javaClass.classLoader.getResourceAsStream(resourceName)
                        ?: throw IllegalStateException("Failed to locate $resourceName on the classpath")

                try {
                    return parser.apply(`is`)
                } finally {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                    }

                }
            }

        }

        val load = qpd.load(context)

        assertNotNull(load)
        return load
    }
}