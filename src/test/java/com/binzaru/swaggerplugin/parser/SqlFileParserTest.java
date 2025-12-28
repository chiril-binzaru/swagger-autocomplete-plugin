package com.binzaru.swaggerplugin.parser;

import java.util.stream.Stream;
import java.io.InputStream;

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

    @ParameterizedTest
    @MethodSource("provideExtractSwaggerPathTestData")
    public void testExtractSwaggerPath(String sql, String expectedSwaggerPath) {
        String actualSwaggerPath = SqlFileParser.extractSwaggerPath(sql);
        assertEquals(expectedSwaggerPath, actualSwaggerPath);
    }

    @ParameterizedTest
    @MethodSource("provideExtractAliasPathTestData")
    public void testExtractAliasPath(String textBeforeCursor, String[] expectedAliasPath) {
        String[] actualAliasPath = SqlFileParser.extractAliasPath(textBeforeCursor);
        assertArrayEquals(expectedAliasPath, actualAliasPath);
    }



    private static Stream<Arguments> provideExtractModelNameTestData() {
        return Stream.of(
                Arguments.of(
                        loadSqlFile("sql/parser/extract_model_name/valid_model_user.sql"),
                        "User"
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/extract_model_name/valid_model_before_last_row.sql"),
                        "Elephant"
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/extract_model_name/valid_model_last_row.sql"),
                        "Zebra"
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/extract_model_name/valid_model_after_last_row.sql"),
                        null
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/extract_model_name/no_model.sql"),
                        null
                )
        );
    }

    private static Stream<Arguments> provideExtractSwaggerPathTestData() {
        return Stream.of(
                Arguments.of(
                        loadSqlFile("sql/parser/extract_swagger_path/valid_swagger_relative.sql"),
                        "./swagger.json"
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/extract_swagger_path/valid_swagger_absolute.sql"),
                        "/api/swagger.json"
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/extract_swagger_path/valid_swagger_before_last_row.sql"),
                        "D:\\folder\\subfolder\\swagger.json"
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/extract_swagger_path/valid_swagger_last_row.sql"),
                        "D:\\folder\\subfolder\\swagger.json"
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/extract_swagger_path/valid_swagger_after_last_row.sql"),
                        null
                ),
                Arguments.of(
                        loadSqlFile("sql/parser/extract_swagger_path/no_swagger.sql"),
                        null
                )
        );
    }

    private static Stream<Arguments> provideExtractAliasPathTestData() {
        return Stream.of(
                Arguments.of(
                        "SELECT u.id AS \"address.",
                        new String[]{"address", ""}
                ),
                Arguments.of(
                        "SELECT u.id AS \"customerId",
                        new String[]{"customerId"}
                ),
                Arguments.of(
                        "SELECT u.id AS address.city",
                        new String[]{"address", "city"}
                ),
                Arguments.of(
                        "SELECT u.id AS \"loyaltyProgramMembership.membershipTierLevel",
                        new String[]{"loyaltyProgramMembership", "membershipTierLevel"}
                ),
                Arguments.of(
                        "SELECT u.id",
                        new String[0]
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
