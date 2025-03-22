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
        SchemaDto dto = objectMapper.readValue(s, SchemaDto.class);
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
        SchemaDto dto = objectMapper.readValue(s, SchemaDto.class);
        assertEquals("testSchema", dto.getName());
        Set<SchemaPropertyDto> properties = dto.getProperties();
        assertEquals(1, properties.size());
        SchemaPropertyDto propertyDto = properties.iterator().next();
        assertEquals("testProperty1", propertyDto.getName());
    }

}
