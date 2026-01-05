package com.binzaru.swaggerplugin.completion;

import com.binzaru.swaggerplugin.parser.SqlFileParser;
import com.binzaru.swaggerplugin.parser.SwaggerCache;
import com.binzaru.swaggerplugin.parser.SwaggerParser;
import com.binzaru.swaggerplugin.patterns.Patterns;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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

                        // Check if we are on a line with @model:
                        String lineText = getLineText(parameters);
                        if (!Patterns.MODEL_COMPLETION_TRIGGER.matcher(lineText).matches()) {
                            return;
                        }

                        // Get the Swagger file path
                        String swaggerPath = SqlFileParser.extractSwaggerPath(file.getText());
                        if (swaggerPath == null) {
                            return;
                        }

                        File swaggerFile = SqlFileParser.resolveSwaggerPath(swaggerPath, file.getVirtualFile());

                        // Load Swagger from cache or parse anew
                        SwaggerParser swaggerParser = SwaggerCache.getOrLoad(swaggerFile);
                        if (swaggerParser == null) {
                            return;
                        }

                        // Add all models to autocompletion with priority to preserve order
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