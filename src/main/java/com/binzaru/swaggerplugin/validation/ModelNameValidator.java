package com.binzaru.swaggerplugin.validation;

import com.binzaru.swaggerplugin.parser.SqlFileParser;
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

public class ModelNameValidator implements Annotator {

    private final SwaggerParser parser = new SwaggerParser();

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element.getFirstChild() != null) {
            return;
        }

        String text = element.getText();
        if (!text.contains("@model")) {
            return;
        }

        Matcher matcher = Patterns.MODEL_ANNOTATION.matcher(text);
        if (!matcher.find()) {
            return;
        }

        String modelName = matcher.group(1);
        int start = element.getTextRange().getStartOffset() + matcher.start(1);
        int end = element.getTextRange().getStartOffset() + matcher.end(1);
        TextRange range = new TextRange(start, end);

        // Load Swagger
        String swaggerPath = SqlFileParser.extractSwaggerPath(element.getContainingFile().getText());
        if (swaggerPath == null) {
            return;
        }

        // Resolve path
        File swaggerFile = SqlFileParser.resolveSwaggerPath(
                swaggerPath,
                element.getContainingFile().getVirtualFile()
        );

        if (!swaggerFile.exists()) {
            return; // Swagger not found - skip model validation
        }

        // Load Swagger and validate model
        try {
            parser.loadSwagger(swaggerFile.getAbsolutePath());

            if (parser.getModel(modelName) != null) {
                // Model found - green highlight
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD)
                        .create();
            } else {
                // Model not found - error
                holder.newAnnotation(HighlightSeverity.ERROR, "Model '" + modelName + "' not found in Swagger schema")
                        .range(range)
                        .create();
            }
        } catch (Exception e) {
            // Swagger loading error - skip validation
        }
    }
}