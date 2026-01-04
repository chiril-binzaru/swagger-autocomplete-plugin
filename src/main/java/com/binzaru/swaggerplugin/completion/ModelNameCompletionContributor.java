package com.binzaru.swaggerplugin.completion;

import com.binzaru.swaggerplugin.parser.SqlFileParser;
import com.binzaru.swaggerplugin.parser.SwaggerParser;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;

public class ModelNameCompletionContributor extends CompletionContributor {

    public ModelNameCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new CompletionProvider<>() {
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

                        File swaggerFile = SqlFileParser.resolveSwaggerPath(swaggerPath, file.getVirtualFile());

                        SwaggerParser swaggerParser = new SwaggerParser();
                        try {
                            swaggerParser.loadSwagger(swaggerFile.getAbsolutePath());
                        } catch (FileNotFoundException e) {
                            return;
                        }

                        // Добавляем все модели в автодополнение с приоритетом для сохранения порядка
                        int priority = Integer.MAX_VALUE;
                        for (String modelName : swaggerParser.getAllModelNames()) {
                            result.addElement(
                                    PrioritizedLookupElement.withPriority(
                                            LookupElementBuilder.create(modelName)
                                                    .withTypeText("Swagger Model")
                                                    .withCaseSensitivity(false),
                                            priority--
                                    )
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