package org.mivirim.graph.schema.controller;

import org.mivirim.graph.schema.dto.SchemaDto;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schema")
public class SchemaController {

    @PutMapping
    void upsertSchema(@RequestBody SchemaDto schemaDto) {

    }

}
