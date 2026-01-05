package com.binzaru.swaggerplugin.parser;

import java.io.File;
import java.util.stream.Stream;
import java.io.InputStream;

import com.intellij.openapi.vfs.VirtualFile;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SqlFileParserTest {

    @ParameterizedTest
    @MethodSource("provideExtractModelNameTestData")
    public void extractModelNameTest(String sql, String expectedModelName) {
        String actualModelName = SqlFileParser.extractModelName(sql);
        assertEquals(expectedModelName, actualModelName);
    }


    @ParameterizedTest
    @MethodSource("provideExtractSwaggerPathTestData")
    public void extractSwaggerPathTest(String sql, String expectedSwaggerPath) {
        String actualSwaggerPath = SqlFileParser.extractSwaggerPath(sql);
        assertEquals(expectedSwaggerPath, actualSwaggerPath);
    }


    @ParameterizedTest
    @MethodSource("provideExtractAliasPathTestData")
    public void extractAliasPathTest(String textBeforeCursor, String[] expectedAliasPath) {
        String[] actualAliasPath = SqlFileParser.extractAliasPath(textBeforeCursor);
        assertArrayEquals(expectedAliasPath, actualAliasPath);
    }


    @Tag("intellij-platform")
    @ParameterizedTest
    @MethodSource("provideResolveRelativeSwaggerPathTestData")
    public void resolveRelativeSwaggerPathTest(String swaggerPath, String expectedPath) {
        // Мокируем VirtualFile
        VirtualFile mockCurrentFile = mock(VirtualFile.class);
        VirtualFile mockParentDir = mock(VirtualFile.class);

        when(mockCurrentFile.getParent()).thenReturn(mockParentDir);
        when(mockParentDir.getPath()).thenReturn("C:/project/queries");

        File result = SqlFileParser.resolveSwaggerPath(swaggerPath, mockCurrentFile);

        assertEquals(expectedPath, result.getAbsolutePath());
    }

    @Tag("intellij-platform")
    @ParameterizedTest
    @MethodSource("provideResolveAbsoluteSwaggerPathTestData")
    void resolveAbsoluteSwaggerPathTest(String swaggerPath, String expectedPath) {
        VirtualFile mockCurrentFile = mock(VirtualFile.class);
        File result = SqlFileParser.resolveSwaggerPath(swaggerPath, mockCurrentFile);

        assertEquals(expectedPath, result.getAbsolutePath());
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
                        "SELECT u.id AS \"",
                        new String[]{""}
                ),
                Arguments.of(
                        "SELECT u.id AS \"c",
                        new String[]{"c"}
                ),
                Arguments.of(
                        "SELECT u.id AS \"customerId",
                        new String[]{"customerId"}
                ),
                Arguments.of(
                        "SELECT u.id AS \"address.",
                        new String[]{"address", ""}
                ),
                Arguments.of(
                        "SELECT u.id AS \"loyaltyProgramMembership.membershipTierLevel",
                        new String[]{"loyaltyProgramMembership", "membershipTierLevel"}
                ),
                Arguments.of(
                        "SELECT u.id AS \"loyaltyProgramMembership.membershipTierLevel.",
                        new String[]{"loyaltyProgramMembership", "membershipTierLevel", ""}
                ),
                Arguments.of(
                        "SELECT u.id AS ",
                        new String[]{""}
                ),
                Arguments.of(
                        "SELECT u.id AS c",
                        new String[]{"c"}
                ),
                Arguments.of(
                        "SELECT u.id AS customerId",
                        new String[]{"customerId"}
                ),
                Arguments.of(
                        "SELECT u.id AS address.",
                        new String[]{"address", ""}
                ),
                Arguments.of(
                        "SELECT u.id AS loyaltyProgramMembership.membershipTierLevel",
                        new String[]{"loyaltyProgramMembership", "membershipTierLevel"}
                ),
                Arguments.of(
                        "SELECT u.id AS loyaltyProgramMembership.membershipTierLevel.",
                        new String[]{"loyaltyProgramMembership", "membershipTierLevel", ""}
                ),
                Arguments.of(
                        "SELECT u.id",
                        new String[0]
                )
        );
    }


    private static Stream<Arguments> provideResolveRelativeSwaggerPathTestData() {
        return Stream.of(
                Arguments.argumentSet("Current directory",
                        "swagger.json",
                        "C:" + File.separator + "project" + File.separator + "queries" + File.separator + "swagger.json"
                ),

                Arguments.argumentSet("Current directory marker '.' (/ separator)",
                        "./swagger.json",
                        "C:" + File.separator + "project" + File.separator + "queries" + File.separator + "swagger.json"
                ),
                Arguments.argumentSet("Nested directory (/ separator)",
                        "folder/swagger.json",
                        "C:" + File.separator + "project\\queries" + File.separator + "folder" + File.separator + "swagger.json"
                ),
                Arguments.argumentSet("Parent directory marker '..' (/ separator)",
                        "../swagger.json",
                        "C:" + File.separator + "project" + File.separator + "swagger.json"
                ),

                Arguments.argumentSet("Current directory marker '.' (\\ separator)",
                        ".\\swagger.json",
                        "C:" + File.separator + "project" + File.separator + "queries" + File.separator + "swagger.json"
                ),
                Arguments.argumentSet("Nested directory (\\ separator)",
                        "folder\\swagger.json",
                        "C:" + File.separator + "project" + File.separator + "queries" + File.separator + "folder" + File.separator + "swagger.json"
                ),
                Arguments.argumentSet("Parent directory marker '..' (\\ separator)",
                        "..\\swagger.json",
                        "C:" + File.separator + "project" + File.separator + "swagger.json"
                )
        );
    }

    private static Stream<Arguments> provideResolveAbsoluteSwaggerPathTestData() {
        return Stream.of(
                Arguments.argumentSet("Valid absolute path",
                        "D:/User/Documents/swagger.json",
                        "D:" + File.separator + "User" + File.separator + "Documents" + File.separator + "swagger.json"
                ),
                Arguments.argumentSet("Valid absolute path",
                        "D:\\User\\Documents\\swagger.json",
                        "D:" + File.separator + "User" + File.separator + "Documents" + File.separator + "swagger.json"
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
