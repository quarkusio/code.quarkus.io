package io.quarkus.code.service

import io.quarkus.code.misc.QuarkusExtensionUtils
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.stream.Collectors
import javax.inject.Inject

@QuarkusTest
internal class QuarkusExtensionCatalogServiceTest {

    @Inject
    lateinit var catalog: QuarkusExtensionCatalogService

    @Test
    internal fun testFields() {
        assertThat(QuarkusExtensionCatalogService.platformVersion, not(emptyOrNullString()))
        assertThat(QuarkusExtensionCatalogService.catalog, notNullValue())
        assertThat(catalog.extensions, not(empty<CodeQuarkusExtension>()))
    }

    @Test
    @DisplayName("Check that our shortIds are unique")
    fun testUniqueShortIds() {
        val extensions = catalog.extensions
        val uniqueIds = HashSet(extensions.map {it.id})
        val linkIds = uniqueIds.map { QuarkusExtensionUtils.createShortId(it) }
        val duplicates = findDuplicates(linkIds)
        assertThat(duplicates?.size, `is`(0))
    }

    private fun <T> findDuplicates(collection: Collection<T>): Set<T>? {
        val uniques: MutableSet<T> = HashSet()
        return collection.stream()
                .filter { e: T -> !uniques.add(e) }
                .collect(Collectors.toSet())
    }
}