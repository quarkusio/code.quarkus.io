package io.quarkus.code.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

internal class ProjectDefinitionTest {

    @Test
    internal fun testClassNamePattern() {
        assertThat(Pattern.compile(ProjectDefinition.CLASSNAME_PATTERN).asPredicate()).accepts("de.if.ExampleResource")
    }
}