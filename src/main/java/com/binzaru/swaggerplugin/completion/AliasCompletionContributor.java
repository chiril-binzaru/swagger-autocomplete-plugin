package com.binzaru.swaggerplugin.completion;

import com.binzaru.swaggerplugin.parser.SqlFileParser;
import com.binzaru.swaggerplugin.parser.SwaggerCache;
import com.binzaru.swaggerplugin.parser.SwaggerModel;
import com.binzaru.swaggerplugin.parser.SwaggerParser;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

public class AliasCompletionContributor extends CompletionContributor {

    public AliasCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {

                        /*
                            PSI - Program Structure Interface
                            It is a logical, hierarchical representation of a source file in IntelliJ IDEA,
                            exposing its structure and semantics rather than just its raw text.
                        */
                        PsiFile psiFile = parameters.getOriginalFile();

                        // Загружаем Swagger из кэша
                        String swaggerPath = SqlFileParser.extractSwaggerPath(psiFile.getText());
                        if (swaggerPath == null) {
                            return;
                        }

                        File swaggerFile = SqlFileParser.resolveSwaggerPath(swaggerPath, psiFile.getVirtualFile());
                        SwaggerParser swaggerParser = SwaggerCache.getOrLoad(swaggerFile);
                        if (swaggerParser == null) {
                            return;
                        }

                        // Получаем модель из @model
                        String modelName = SqlFileParser.extractModelName(psiFile.getText());
                        if (modelName == null) return;

                        SwaggerModel rootModel = swaggerParser.getModel(modelName);
                        if (rootModel == null) return;

                        // Получаем текст до курсора
                        Document document = FileDocumentManager.getInstance().getDocument(psiFile.getVirtualFile());
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
                        // Сортировщик только по нашим приоритетам (без группировки по релевантности)
                        CompletionSorter sorter = CompletionSorter.emptySorter()
                                .weigh(new LookupElementWeigher("swaggerOrder") {
                                    @Override
                                    public @NotNull Comparable<?> weigh(@NotNull LookupElement element) {
                                        if (element instanceof PrioritizedLookupElement<?> prioritized) {
                                            // Инвертируем: больший приоритет = меньшее значение = выше в списке
                                            return -prioritized.getPriority();
                                        }
                                        return 0;
                                    }
                                });

                        result = result.caseInsensitive()
                                .withPrefixMatcher(prefix)
                                .withRelevanceSorter(sorter);

                        // Определяем какую модель показывать
                        SwaggerModel targetModel = rootModel;

                        if (aliasPath.length > 0) {
                            // Навигируемся по пути (например ["address"])
                            for (int i = 0; i < aliasPath.length - 1; i++) {
                                SwaggerModel.PropertyInfo prop = targetModel.getProperty(aliasPath[i]);
                                if (prop != null && prop.isReference()) {
                                    targetModel = swaggerParser.resolveRef(prop.getRef());
                                    if (targetModel == null) return;
                                } else if (prop == null) {
                                    return;  // Поле не найдено - выходим
                                }
                            }
                        }

                        // Добавляем все поля модели в автодополнение с приоритетом для сохранения порядка
                        int priority = Integer.MAX_VALUE;
                        for (Map.Entry<String, SwaggerModel.PropertyInfo> entry : targetModel.getProperties().entrySet()) {
                            String fieldName = entry.getKey();
                            SwaggerModel.PropertyInfo info = entry.getValue();

                            LookupElementBuilder element = LookupElementBuilder.create(fieldName);

                            if (info.getType() != null) {
                                element = element.withTypeText(info.getType());
                            } else if (info.isReference()) {
                                SwaggerModel refModel = swaggerParser.resolveRef(info.getRef());
                                if (refModel != null) {
                                    element = element.withTypeText(refModel.getName());
                                }
                            }

                            if (info.getDescription() != null) {
                                element = element.withTailText(" - " + info.getDescription(), true);
                            }

                            result.addElement(PrioritizedLookupElement.withPriority(element, priority--));
                        }
                    }
                }
        );
    }
}