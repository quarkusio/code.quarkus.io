package io.quarkus.code.services

import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.services.QuarkusExtensionUtils.processExtensions
import io.quarkus.code.services.QuarkusExtensionUtils.shorten
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
import java.util.function.Function

internal class QuarkusExtensionUtilsTest {

    @Test
    internal fun testShorten() {
        assertThat(shorten("some random long string"), `is`("ODO"))
        assertThat(shorten(""), `is`("a"))
        assertThat(shorten("some-id"), `is`("gLa"))
        assertThat(shorten("io.quarkus:quarkus-arc"), `is`("3eJ"))
    }

    @Test
    internal fun textContent() {
        val extensions = processExtensions(getTestDescriptor())
        assertThat(extensions[0], `is`(CodeQuarkusExtension(
                "io.quarkus:quarkus-arc",
                "zmg",
                "ArC",
                "Build time CDI dependency injection",
                "CDI",
                "Core",
                "stable",
                false,
                listOf("arc", "cdi", "dependency-injection", "di"),
                "https://quarkus.io/guides/cdi-reference",
                0,
                listOf("arc", "cdi", "dependency-injection", "di")))
        )
        assertThat(extensions[5], `is`(CodeQuarkusExtension(
                "io.quarkus:quarkus-netty",
                "rpC",
                "Netty",
                "Netty is a non-blocking I/O client-server framework. Used by Quarkus as foundation layer.",
                null,
                "Web",
                "stable",
                false,
                listOf(),
                null,
                5,
                listOf()))
        )
    }

    @Test
    internal fun testOrder() {
        val extensions = processExtensions(getTestDescriptor())
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


    internal fun getTestDescriptor(): QuarkusJsonPlatformDescriptor {
        val qpd = QuarkusJsonPlatformDescriptorLoaderImpl()

        val artifactResolver = object : ArtifactResolver {

            override fun <T> process(groupId: String, artifactId: String, classifier: String, type: String, version: String,
                                     processor: Function<Path, T>): T {
                throw UnsupportedOperationException()
            }

            override fun getManagedDependencies(groupId: String, artifactId: String, classifier: String?,
                                                type: String, version: String): List<Dependency> {
                return emptyList()
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