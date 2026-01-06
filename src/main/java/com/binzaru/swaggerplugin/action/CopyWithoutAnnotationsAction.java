package com.binzaru.swaggerplugin.action;

import com.binzaru.swaggerplugin.patterns.Patterns;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Action that copies SQL file content to clipboard without @swagger and @model annotation lines.
 * <p>
 * Triggered by Ctrl+Shift+C.
 */
public class CopyWithoutAnnotationsAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (editor == null) {
            return;
        }

        String text = editor.getDocument().getText();
        String filteredText = filterAnnotations(text);

        // Copy to clipboard
        StringSelection selection = new StringSelection(filteredText);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    /**
     * Filters out lines containing @swagger and @model annotations,
     * and removes leading empty lines.
     *
     * @param text the original file content
     * @return text without annotation lines, starting from first non-empty line
     */
    private String filterAnnotations(String text) {
        String filtered = Arrays.stream(text.split("\\r?\\n"))
                .filter(line -> !Patterns.SWAGGER_ANNOTATION.matcher(line).find())
                .filter(line -> !Patterns.MODEL_ANNOTATION.matcher(line).find())
                .collect(Collectors.joining("\n"));

        // Remove leading empty lines
        return filtered.replaceFirst("^\\s*\\n+", "");
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }
}
