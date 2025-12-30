package com.binzaru.swaggerplugin.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class SwaggerParserTest {

    private SwaggerParser swaggerParser = new SwaggerParser();
    private String validSwaggerPath;

    @BeforeEach
    public void setUp() {
        swaggerParser = new SwaggerParser();
        validSwaggerPath = Paths.get("src/test/resources/swagger/test-swagger.json").toString();
    }

    @Test
    public void testLoadValidSwagger() {
        try {
            swaggerParser.loadSwagger(validSwaggerPath);
            assertTrue(swaggerParser.isLoaded());
        } catch (FileNotFoundException e) {
            fail("loadSwagger() should not throw FileNotFoundException for a valid Swagger path");
        }
    }

}
