package io.quarkus.code.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectDefinitionQueryTest {

    @Test
    public void testParseCodestartDataSimple() {
        Map<String, String> result = ProjectDefinitionQuery.parseCodestartData(Set.of("key=value"));
        assertEquals(Map.of("key", "value"), result);
    }

    @Test
    public void testParseCodestartDataDottedKey() {
        Map<String, String> result = ProjectDefinitionQuery.parseCodestartData(Set.of("my.nested.key=myvalue"));
        assertEquals(Map.of("my.nested.key", "myvalue"), result);
    }

    @Test
    public void testParseCodestartDataValueWithEquals() {
        Map<String, String> result = ProjectDefinitionQuery.parseCodestartData(Set.of("key=val=ue"));
        assertEquals(Map.of("key", "val=ue"), result);
    }

    @Test
    public void testParseCodestartDataMultipleEntries() {
        Map<String, String> result = ProjectDefinitionQuery.parseCodestartData(Set.of("a=1", "b.c=2"));
        assertEquals(Map.of("a", "1", "b.c", "2"), result);
    }

    @Test
    public void testParseCodestartDataEmpty() {
        Map<String, String> result = ProjectDefinitionQuery.parseCodestartData(Set.of());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testParseCodestartDataInvalidEntriesIgnored() {
        Map<String, String> result = ProjectDefinitionQuery.parseCodestartData(Set.of("noequals", "=nokey", "valid=entry"));
        assertEquals(Map.of("valid", "entry"), result);
    }
}
