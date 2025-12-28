package com.binzaru.swaggerplugin.completion;

import com.binzaru.swaggerplugin.parser.SqlFileParser;
import com.binzaru.swaggerplugin.parser.SwaggerParser;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class ModelNameCompletionContributor extends CompletionContributor {
    private final SwaggerParser parser = new SwaggerParser();
    private String lastLoadedSwaggerPath = null;

    public ModelNameCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {

                        PsiFile file = parameters.getOriginalFile();

                        // Проверяем что мы в строке с @model:
                        String lineText = getLineText(parameters);
                        if (!lineText.matches(".*--\\s*@model:\\s*\\w*$")) {
                            return;
                        }

                        // Загружаем Swagger
                        String swaggerPath = SqlFileParser.extractSwaggerPath(file.getText());
                        if (swaggerPath == null) {
                            return;
                        }

                        if (!parser.isLoaded() || !swaggerPath.equals(lastLoadedSwaggerPath)) {
                            try {
                                parser.loadSwagger(swaggerPath);
                                lastLoadedSwaggerPath = swaggerPath;
                            } catch (Exception e) {
                                return;
                            }
                        }

                        // Добавляем все модели в автодополнение
                        for (String modelName : parser.getAllModelNames()) {
                            result.addElement(
                                    LookupElementBuilder.create(modelName)
                                            .withTypeText("Swagger Model")
                                            .withCaseSensitivity(false)
                            );
                        }
                    }

                    private String getLineText(CompletionParameters parameters) {
                        int offset = parameters.getOffset();
                        String text = parameters.getOriginalFile().getText();
                        int lineStart = text.lastIndexOf('\n', offset - 1) + 1;
                        return text.substring(lineStart, offset);
                    }
                }
        );
    }
}