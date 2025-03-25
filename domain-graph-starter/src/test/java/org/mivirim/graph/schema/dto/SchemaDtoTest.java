package org.mivirim.graph.schema.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchemaDtoTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testEmptySchema() throws JsonProcessingException {
        var s = """
                    { "name": "testSchema" }
                """;
        EntityDefinitionDto dto = objectMapper.readValue(s, EntityDefinitionDto.class);
        assertEquals("testSchema", dto.getName());
    }

    @Test
    void testSchemaWithProperties() throws JsonProcessingException {
        var s = """
                    { 
                        "name": "testSchema",
                        "properties": [
                            {
                                "name": "testProperty1"
                            }
                        ]
                    }
                """;
        EntityDefinitionDto dto = objectMapper.readValue(s, EntityDefinitionDto.class);
        assertEquals("testSchema", dto.getName());
        Set<PropertyDefinitionDto> properties = dto.getProperties();
        assertEquals(1, properties.size());
        PropertyDefinitionDto propertyDto = properties.iterator().next();
        assertEquals("testProperty1", propertyDto.getName());
    }

}
