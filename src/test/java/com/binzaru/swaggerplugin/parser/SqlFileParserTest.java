package com.binzaru.swaggerplugin.parser;

import java.util.stream.Stream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class SqlFileParserTest {

    @ParameterizedTest
    @MethodSource("provideExtractModelNameTestData")
    public void testExtractModelName(String sql, String expectedModelName) {
        String actualModelName = SqlFileParser.extractModelName(sql);
        assertEquals(expectedModelName, actualModelName);
    }



    private static Stream<Arguments> provideExtractModelNameTestData() {
        return Stream.of(
                Arguments.of(
                        loadSqlFile("sql/parser/valid_model_user.sql"),
                        "User"
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/before_last_row_model.sql"),
                        "Elephant"
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/last_row_model.sql"),
                        "Zebra"
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/after_last_row_model.sql"),
                        null
                )
        );
    }



    private static String loadSqlFile(String path) {
        try (InputStream is = SqlFileParserTest.class.getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load: " + path, e);
        }
    }

}
