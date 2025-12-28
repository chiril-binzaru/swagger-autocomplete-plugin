package com.binzaru.swaggerplugin.completion;

import com.binzaru.swaggerplugin.parser.SqlFileParser;
import com.binzaru.swaggerplugin.parser.SwaggerModel;
import com.binzaru.swaggerplugin.parser.SwaggerParser;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AliasCompletionContributor extends CompletionContributor {

    /**
     * Parser that loads and stores Swagger/OpenAPI schema models from .json file.
     * Holds parsed model structures in memory for autocomplete suggestions.
     */
    private final SwaggerParser parser = new SwaggerParser();

    private String lastLoadedSwaggerPath = null;

    public AliasCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {

                        /*
                            PSI - Program Structure Interface
                            It is a logical, hierarchical representation of a source file in IntelliJ IDEA,
                            exposing its structure and semantics rather than just its raw text.
                        */
                        PsiFile file = parameters.getOriginalFile();

                        // Загружаем Swagger если еще не загружен
                        String swaggerPath = SqlFileParser.extractSwaggerPath(file.getText());

                        if (!parser.isLoaded() || !swaggerPath.equals(lastLoadedSwaggerPath)) {
                            if (swaggerPath != null) {
                                try {
                                    parser.loadSwagger(swaggerPath);
                                    lastLoadedSwaggerPath = swaggerPath;
                                } catch (Exception e) {
                                    return;
                                }
                            }
                        }

                        // Получаем модель из @model
                        String modelName = SqlFileParser.extractModelName(file.getText());
                        if (modelName == null) return;

                        SwaggerModel rootModel = parser.getModel(modelName);
                        if (rootModel == null) return;

                        // Получаем текст до курсора
                        Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
                        if (document == null) return;

                        int offset = parameters.getOffset();
                        int lineStart = document.getLineStartOffset(document.getLineNumber(offset));
                        String textBeforeCursor = document.getText().substring(lineStart, offset);

                        // Парсим путь алиаса
                        String[] aliasPath = SqlFileParser.extractAliasPath(textBeforeCursor);

                        // Проверяем что есть AS в строке
                        if (!textBeforeCursor.toUpperCase().contains(" AS ")) {
                            return;
                        }

                        // Извлекаем префикс для фильтрации
                        String prefix = "";
                        if (aliasPath.length > 0) {
                            String lastPart = aliasPath[aliasPath.length - 1];
                            if (!lastPart.isEmpty()) {
                                prefix = lastPart;
                            }
                        }
                        result = result.caseInsensitive().withPrefixMatcher(prefix);

                        // Определяем какую модель показывать
                        SwaggerModel targetModel = rootModel;

                        if (aliasPath.length > 0) {
                            // Навигируемся по пути (например ["address"])
                            for (int i = 0; i < aliasPath.length - 1; i++) {
                                SwaggerModel.PropertyInfo prop = targetModel.getProperty(aliasPath[i]);
                                if (prop != null && prop.isReference()) {
                                    targetModel = parser.resolveRef(prop.getRef());
                                    if (targetModel == null) return;
                                } else if (prop == null) {
                                    return;  // Поле не найдено - выходим
                                }
                            }
                        }

                        // Добавляем все поля модели в автодополнение
                        for (Map.Entry<String, SwaggerModel.PropertyInfo> entry : targetModel.getProperties().entrySet()) {
                            String fieldName = entry.getKey();
                            SwaggerModel.PropertyInfo info = entry.getValue();

                            LookupElementBuilder element = LookupElementBuilder.create(fieldName);

                            if (info.getType() != null) {
                                element = element.withTypeText(info.getType());
                            } else if (info.isReference()) {
                                SwaggerModel refModel = parser.resolveRef(info.getRef());
                                if (refModel != null) {
                                    element = element.withTypeText(refModel.getName());
                                }
                            }

                            if (info.getDescription() != null) {
                                element = element.withTailText(" - " + info.getDescription(), true);
                            }

                            result.addElement(element);
                        }
                    }
                }
        );
    }
}