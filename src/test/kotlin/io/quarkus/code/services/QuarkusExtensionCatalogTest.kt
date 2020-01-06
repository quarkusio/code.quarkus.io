package io.quarkus.code.services

import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test

@QuarkusTest
internal class QuarkusExtensionCatalogTest {

    @Test
    internal fun testFields() {
        assertThat(QuarkusExtensionCatalog.platformVersion, not(emptyOrNullString()))
        assertThat(QuarkusExtensionCatalog.bundledQuarkusVersion, not(emptyOrNullString()))
        assertThat(QuarkusExtensionCatalog.descriptor, notNullValue())
        assertThat(QuarkusExtensionCatalog.processedExtensions, not(empty<CodeQuarkusExtension>()))
    }
}