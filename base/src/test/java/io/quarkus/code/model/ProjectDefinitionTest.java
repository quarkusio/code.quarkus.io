import io.quarkus.code.model.ProjectDefinition;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectDefinitionTest {

    @Test
    public void testClassNamePattern() {
        assertTrue(Pattern.compile(ProjectDefinition.CLASSNAME_PATTERN).asPredicate().test("de.if.ExampleResource"));
    }
}