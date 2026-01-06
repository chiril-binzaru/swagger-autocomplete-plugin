package com.binzaru.swaggerplugin.action;

import com.binzaru.swaggerplugin.parser.SqlFileParser;
import com.binzaru.swaggerplugin.parser.SwaggerCache;
import com.binzaru.swaggerplugin.parser.SwaggerModel;
import com.binzaru.swaggerplugin.parser.SwaggerParser;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Action that generates a SELECT statement with all fields from a Swagger model.
 * Recursively expands nested $ref models into dot-notation paths.
 * <p>
 * Triggered by Ctrl+G in SQL files that contain @swagger and @model annotations.
 */
public class GenerateSelectAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (project == null || editor == null || psiFile == null) {
            return;
        }

        String fileText = psiFile.getText();

        // Extract @swagger path
        String swaggerPath = SqlFileParser.extractSwaggerPath(fileText);
        if (swaggerPath == null) {
            return;
        }

        // Load swagger
        File swaggerFile = SqlFileParser.resolveSwaggerPath(swaggerPath, psiFile.getVirtualFile());
        SwaggerParser swaggerParser = SwaggerCache.getOrLoad(swaggerFile);
        if (swaggerParser == null) {
            return;
        }

        // Extract @model name
        String modelName = SqlFileParser.extractModelName(fileText);
        if (modelName == null) {
            return;
        }

        SwaggerModel rootModel = swaggerParser.getModel(modelName);
        if (rootModel == null) {
            return;
        }

        // Generate SELECT statement
        String selectStatement = generateSelect(rootModel, swaggerParser);

        // Insert at cursor position and move caret after FROM
        int offset = editor.getCaretModel().getOffset();
        WriteCommandAction.runWriteCommandAction(project, () ->
            editor.getDocument().insertString(offset, selectStatement));

        // Move caret to end (after "FROM ")
        editor.getCaretModel().moveToOffset(offset + selectStatement.length());
    }

    /**
     * Generates a SELECT statement with all fields from the model.
     * Recursively expands $ref references into dot-notation paths.
     */
    private String generateSelect(SwaggerModel model, SwaggerParser swaggerParser) {
        List<String> fields = new ArrayList<>();
        collectFields(model, swaggerParser, "", fields, new HashSet<>());

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT\n");

        for (int i = 0; i < fields.size(); i++) {
            sb.append("     AS ").append(fields.get(i));
            if (i < fields.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("FROM ");

        return sb.toString();
    }

    /**
     * Recursively collects all field paths from a model.
     *
     * @param model         the current model to process
     * @param swaggerParser parser for resolving $ref references
     * @param prefix        current path prefix (e.g., "billingAddress.")
     * @param fields        list to collect field paths into
     * @param visited       set of visited model names to prevent infinite recursion
     */
    private void collectFields(SwaggerModel model, SwaggerParser swaggerParser,
                               String prefix, List<String> fields, Set<String> visited) {
        // Prevent infinite recursion on circular references
        if (visited.contains(model.getName())) {
            return;
        }
        visited.add(model.getName());

        for (Map.Entry<String, SwaggerModel.PropertyInfo> entry : model.getProperties().entrySet()) {
            String fieldName = entry.getKey();
            SwaggerModel.PropertyInfo info = entry.getValue();
            String fullPath = prefix.isEmpty() ? fieldName : prefix + fieldName;

            if (info.isReference()) {
                // Expand nested model
                SwaggerModel nestedModel = swaggerParser.resolveRef(info.getRef());
                if (nestedModel != null) {
                    collectFields(nestedModel, swaggerParser, fullPath + ".", fields, visited);
                }
            } else {
                // Primitive field - add to list
                fields.add(fullPath);
            }
        }

        // Remove from visited to allow same model at different paths
        visited.remove(model.getName());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Enable action only when editor and file are available
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        boolean enabled = project != null && editor != null && psiFile != null;

        if (enabled) {
            String fileText = psiFile.getText();
            // Check if file has @swagger and @model annotations
            enabled = SqlFileParser.extractSwaggerPath(fileText) != null
                    && SqlFileParser.extractModelName(fileText) != null;
        }

        e.getPresentation().setEnabledAndVisible(enabled);
    }
}
