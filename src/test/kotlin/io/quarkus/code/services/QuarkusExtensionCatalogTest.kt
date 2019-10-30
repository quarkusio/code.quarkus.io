package io.quarkus.code.services

import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.services.QuarkusExtensionUtils.processExtensions
import io.quarkus.platform.descriptor.loader.json.ArtifactResolver
import io.quarkus.platform.descriptor.loader.json.QuarkusJsonPlatformDescriptorLoaderContext
import io.quarkus.platform.descriptor.loader.json.impl.QuarkusJsonPlatformDescriptor
import io.quarkus.platform.descriptor.loader.json.impl.QuarkusJsonPlatformDescriptorLoaderImpl
import io.quarkus.platform.tools.DefaultMessageWriter
import io.quarkus.platform.tools.MessageWriter
import org.apache.maven.model.Dependency
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import java.util.ArrayList
import java.util.function.Function
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*

internal class QuarkusExtensionCatalogTest {

    @Test
    internal fun textContent() {
        val extensions = processExtensions(getTestDescriptor())
        assertThat(extensions[0], `is`(CodeQuarkusExtension(
                "io.quarkus:quarkus-arc",
                "ArC",
                "Build time CDI dependency injection",
                "CDI",
                "Core",
                "stable",
                false,
                listOf("arc", "cdi", "dependency-injection", "di"),
                0,
                listOf("arc", "cdi", "dependency-injection", "di")))
        )
        assertThat(extensions[5], `is`(CodeQuarkusExtension(
                "io.quarkus:quarkus-netty",
                "Netty",
                "Netty is a non-blocking I/O client-server framework. Used by Quarkus as foundation layer.",
                null,
                "Web",
                "stable",
                false,
                listOf(),
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
        val context = object : QuarkusJsonPlatformDescriptorLoaderContext {
            internal var mw: MessageWriter = DefaultMessageWriter()

            override fun getMessageWriter(): MessageWriter {
                return mw
            }

            override fun getArtifactResolver(): ArtifactResolver {
                return object : ArtifactResolver {

                    override fun getManagedDependencies(groupId: String, artifactId: String,
                                                        version: String): List<Dependency> {
                        val lx = ArrayList<Dependency>()

                        val core = Dependency()
                        core.artifactId = "quarkus-core"
                        core.groupId = "io.quarkus"
                        core.version = "I don't care!"
                        lx.add(core)
                        return lx
                    }

                    override fun <T> process(groupId: String, artifactId: String, classifier: String, type: String,
                                             version: String, processor: Function<Path, T>): T? {
                        // TODO Auto-generated method stub
                        return null
                    }
                }

            }

            override fun <T> parseJson(parser: Function<Path, T>): T {
                val resourceName = "fakeextensions.json"

                val classLoader = javaClass.classLoader
                val file = File(classLoader.getResource(resourceName)!!.file)

                return parser.apply(file.toPath())
            }
        }
        val load = qpd.load(context)
        assertNotNull(load)
        return load
    }
}