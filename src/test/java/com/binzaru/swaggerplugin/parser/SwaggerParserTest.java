package com.binzaru.swaggerplugin.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class SwaggerParserTest {

    private SwaggerParser swaggerParser = new SwaggerParser();
    private String validSwaggerPath;
    private final String invalidSwaggerPath = "C:" + File.separator + "non-existent-folder" + File.separator + "non-existent-swagger.json";

    @BeforeEach
    public void setUp() {
        swaggerParser = new SwaggerParser();
        validSwaggerPath = new File("src/test/resources/swagger/test-swagger.json").getAbsolutePath();
    }

    @Test
    public void testLoadExistentSwagger() {
        try {
            swaggerParser.loadSwagger(validSwaggerPath);
            assertTrue(swaggerParser.isLoaded());
        } catch (FileNotFoundException e) {
            fail("loadSwagger() should not throw FileNotFoundException for a valid Swagger path");
        }
    }

    @Test
    public void testLoadNonExistentSwagger() {
        assertThrows(FileNotFoundException.class, () -> swaggerParser.loadSwagger(invalidSwaggerPath));
    }

}
