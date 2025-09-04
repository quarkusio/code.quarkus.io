package io.quarkus.code.misc;

import io.quarkus.code.model.CodeQuarkusExtension;
import io.quarkus.registry.catalog.ExtensionCatalog;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions;
import static io.quarkus.code.misc.QuarkusExtensionUtils.toShortcut;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class QuarkusExtensionUtilsTest {
    private static final String FAKE_CATALOG_JSON = "/fakeextensions.json";

    @Test
    void testToShortcut() {
        assertThat(toShortcut("io.quarkus:quarkus-my-ext"), is("my-ext"));
        assertThat(toShortcut("org.apache.camel.quarkus:camel-quarkus-core"), is("camel-quarkus-core"));
        assertThat(toShortcut("camel-quarkus-core"), is("camel-quarkus-core"));
        assertThat(toShortcut("quarkus-my-ext"), is("my-ext"));
        assertThat(toShortcut("io.quarkiverse.myext:quarkus-my-ext"), is("my-ext"));
    }

    @Test
    void textContent() throws IOException {
        List<CodeQuarkusExtension> extensions = processExtensions(getTestCatalog());
        assertThat(
                extensions.get(0), is(CodeQuarkusExtension.builder()
                        .id("io.quarkus:quarkus-resteasy")
                        .shortId("ignored")
                        .version("5.5.0.1")
                        .name("RESTEasy JAX-RS")
                        .description("REST endpoint framework implementing JAX-RS and more")
                        .shortName("jax-rs")
                        .category("Web")
                        .tags(List.of("with:starter-code", "status:stable"))
                        .keywords(Set.of("endpoint", "framework", "jax", "jaxrs", "jax-rs", "quarkus-resteasy", "rest",
                                "resteasy", "web"))
                        .providesExampleCode(true)
                        .providesCode(true)
                        .guide("https://quarkus.io/guides/rest-json")
                        .order(0)
                        .platform(true)
                        .bom("io.quarkus:quarkus-bom:5.5.0.1")
                        .build()));
        assertThat(extensions.get(6), is(CodeQuarkusExtension.builder()
                .id("io.quarkus:quarkus-rest-client-mutiny")
                .shortId("ignored")
                .version("5.5.0.1")
                .name("Mutiny support for REST Client")
                .description("Enable Mutiny for the REST client")
                .category("Web")
                .tags(List.of("status:preview"))
                .keywords(Set.of("rest", "reactive", "web", "web-client", "rest-client", "client", "quarkus-rest-client-mutiny",
                        "microprofile-rest-client", "support", "mutiny", "rest-client-mutiny"))
                .providesExampleCode(false)
                .providesCode(false)
                .guide(null)
                .order(6)
                .platform(true)
                .bom("io.quarkus:quarkus-bom:5.5.0.1")
                .build()));
    }

    @Test
    void testOrder() throws IOException {
        List<CodeQuarkusExtension> extensions = processExtensions(getTestCatalog());
        assertThat(
                extensions.stream().map(CodeQuarkusExtension::name).toList().subList(0, 5), contains(
                        "RESTEasy JAX-RS",
                        "RESTEasy Jackson",
                        "RESTEasy JSON-B",
                        "Eclipse Vert.x GraphQL",
                        "gRPC"));
    }

    private ExtensionCatalog getTestCatalog() throws IOException {
        var inputString = this.getClass().getResourceAsStream(FAKE_CATALOG_JSON);
        if (inputString == null) {
            throw new IllegalStateException("Failed to locate " + FAKE_CATALOG_JSON + " on the classpath");
        }
        return ExtensionCatalog.fromStream(inputString);
    }
}
