package com.binzaru.swaggerplugin.validation;

import com.binzaru.swaggerplugin.parser.SqlFileParser;
import com.binzaru.swaggerplugin.parser.SwaggerCache;
import com.binzaru.swaggerplugin.parser.SwaggerModel;
import com.binzaru.swaggerplugin.parser.SwaggerParser;
import com.binzaru.swaggerplugin.patterns.Patterns;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.regex.Matcher;

/**
 * Validates alias paths in SQL files against Swagger model structure.
 * <p>
 * Checks that alias paths like "billingAddress.streetName" are valid
 * by verifying each segment exists in the model hierarchy.
 */
public class AliasValidator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // Only process leaf elements
        if (element.getFirstChild() != null) {
            return;
        }

        String text = element.getText();

        // Quick check - must contain AS
        if (!text.toUpperCase().contains(" AS ")) {
            return;
        }

        // Load Swagger
        String fileText = element.getContainingFile().getText();
        String swaggerPath = SqlFileParser.extractSwaggerPath(fileText);
        if (swaggerPath == null) {
            return;
        }

        File swaggerFile = SqlFileParser.resolveSwaggerPath(
                swaggerPath,
                element.getContainingFile().getVirtualFile()
        );

        SwaggerParser swaggerParser = SwaggerCache.getOrLoad(swaggerFile);
        if (swaggerParser == null) {
            return;
        }

        // Get root model
        String modelName = SqlFileParser.extractModelName(fileText);
        if (modelName == null) {
            return;
        }

        SwaggerModel rootModel = swaggerParser.getModel(modelName);
        if (rootModel == null) {
            return;
        }

        // Find all aliases in the element
        Matcher matcher = Patterns.ALIAS_VALIDATION.matcher(text);
        int elementStart = element.getTextRange().getStartOffset();

        while (matcher.find()) {
            String quote = matcher.group(1);
            String alias = matcher.group(2);
            boolean isQuoted = !quote.isEmpty();

            int aliasStart = elementStart + matcher.start(2);
            int aliasEnd = elementStart + matcher.end(2);
            TextRange range = new TextRange(aliasStart, aliasEnd);

            // Validate the alias path
            ValidationResult result = validateAliasPath(alias, rootModel, swaggerParser);

            if (result.isValid()) {
                // Valid alias - green highlight only if not quoted
                if (!isQuoted) {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(range)
                            .textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD)
                            .create();
                }
            } else {
                // Invalid alias - error with message
                holder.newAnnotation(HighlightSeverity.ERROR, result.getErrorMessage())
                        .range(range)
                        .create();
            }
        }
    }

    /**
     * Validates an alias path against the model structure.
     *
     * @param alias         the alias path (e.g., "billingAddress.streetName")
     * @param rootModel     the root Swagger model
     * @param swaggerParser parser for resolving $ref references
     * @return validation result with error message if invalid
     */
    private ValidationResult validateAliasPath(String alias, SwaggerModel rootModel, SwaggerParser swaggerParser) {
        boolean endsWithDot = alias.endsWith(".");
        String pathToValidate = endsWithDot ? alias.substring(0, alias.length() - 1) : alias;

        String[] parts = pathToValidate.split("\\.");
        SwaggerModel currentModel = rootModel;

        for (int i = 0; i < parts.length; i++) {
            String fieldName = parts[i];
            SwaggerModel.PropertyInfo prop = currentModel.getProperty(fieldName);

            if (prop == null) {
                return ValidationResult.error(
                        "Field '" + fieldName + "' not found in model " + currentModel.getName()
                );
            }

            boolean isLastPart = (i == parts.length - 1);

            if (!isLastPart) {
                // Not the last part - must be a reference to navigate further
                if (!prop.isReference()) {
                    return ValidationResult.error(
                            "Field '" + fieldName + "' is not an object, cannot access nested properties"
                    );
                }

                currentModel = swaggerParser.resolveRef(prop.getRef());
                if (currentModel == null) {
                    return ValidationResult.error(
                            "Cannot resolve reference for field '" + fieldName + "'"
                    );
                }
            } else if (endsWithDot) {
                // Last part with trailing dot - must be a reference
                if (!prop.isReference()) {
                    return ValidationResult.error(
                            "Field '" + fieldName + "' is not an object, cannot access nested properties"
                    );
                }
                // It's a reference but path is incomplete
                return ValidationResult.error("Incomplete alias path");
            }
        }

        return ValidationResult.valid();
    }

    /**
     * Result of alias validation.
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        boolean isValid() {
            return valid;
        }

        String getErrorMessage() {
            return errorMessage;
        }
    }
}
