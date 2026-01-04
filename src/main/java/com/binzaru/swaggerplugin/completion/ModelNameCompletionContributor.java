package com.binzaru.swaggerplugin.completion;

import com.binzaru.swaggerplugin.parser.SqlFileParser;
import com.binzaru.swaggerplugin.parser.SwaggerParser;
import com.binzaru.swaggerplugin.patterns.Patterns;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentHashMap;

public class ModelNameCompletionContributor extends CompletionContributor {

    private static final ConcurrentHashMap<String, CachedSwagger> cache = new ConcurrentHashMap<>();

    private static class CachedSwagger {
        final long fileLastModified;
        final SwaggerParser swaggerParser;

        CachedSwagger(long fileLastModified, SwaggerParser swaggerParser) {
            this.fileLastModified = fileLastModified;
            this.swaggerParser = swaggerParser;
        }
    }

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
                        if (!Patterns.MODEL_COMPLETION_TRIGGER.matcher(lineText).matches()) {
                            return;
                        }

                        // Получаем путь к Swagger файлу
                        String swaggerPath = SqlFileParser.extractSwaggerPath(file.getText());
                        if (swaggerPath == null) {
                            return;
                        }

                        File swaggerFile = SqlFileParser.resolveSwaggerPath(swaggerPath, file.getVirtualFile());
                        String absolutePath = swaggerFile.getAbsolutePath();

                        // Загружаем Swagger из кэша или парсим заново
                        SwaggerParser swaggerParser = getOrLoadSwagger(absolutePath, swaggerFile);
                        if (swaggerParser == null) {
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

    private static SwaggerParser getOrLoadSwagger(String absolutePath, File swaggerFile) {
        long currentLastModified = swaggerFile.lastModified();

        CachedSwagger cached = cache.compute(absolutePath, (path, existing) -> {
            // Если кэш валиден — возвращаем как есть
            if (existing != null && existing.fileLastModified == currentLastModified) {
                return existing;
            }

            // Иначе парсим заново
            SwaggerParser newParser = new SwaggerParser();
            try {
                newParser.loadSwagger(path);
                return new CachedSwagger(currentLastModified, newParser);
            } catch (FileNotFoundException e) {
                return null;
            }
        });

        return cached != null ? cached.swaggerParser : null;
    }
}