package com.binzaru.swaggerplugin.action;

import com.binzaru.swaggerplugin.patterns.Patterns;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Action that toggles double quotes on all SQL aliases.
 * <p>
 * Logic:
 * - If all aliases have quotes: remove quotes from all
 * - Otherwise (all without quotes or mixed): add quotes to all
 * <p>
 * Triggered by Ctrl+Shift+Q.
 */
public class ToggleAliasQuotesAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (project == null || editor == null) {
            return;
        }

        Document document = editor.getDocument();
        String text = document.getText();

        // Find all aliases and check their quote status
        List<AliasMatch> matches = findAllAliases(text);

        if (matches.isEmpty()) {
            return;
        }

        // Determine action: if all have quotes -> remove, otherwise -> add
        boolean allHaveQuotes = matches.stream().allMatch(m -> m.hasQuotes);
        boolean shouldAddQuotes = !allHaveQuotes;

        // Apply changes in reverse order to preserve positions
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (int i = matches.size() - 1; i >= 0; i--) {
                AliasMatch match = matches.get(i);
                String replacement = buildReplacement(match, shouldAddQuotes);
                document.replaceString(match.start, match.end, replacement);
            }
        });
    }

    private List<AliasMatch> findAllAliases(String text) {
        List<AliasMatch> matches = new ArrayList<>();
        Matcher matcher = Patterns.ALIAS_WITH_QUOTES.matcher(text);

        while (matcher.find()) {
            String whitespace = matcher.group(1);
            String openQuote = matcher.group(2);
            String alias = matcher.group(3);
            String closeQuote = matcher.group(4);

            boolean hasQuotes = !openQuote.isEmpty() || !closeQuote.isEmpty();

            matches.add(new AliasMatch(
                    matcher.start(),
                    matcher.end(),
                    whitespace,
                    alias,
                    hasQuotes
            ));
        }

        return matches;
    }

    private String buildReplacement(AliasMatch match, boolean addQuotes) {
        if (addQuotes) {
            return "AS" + match.whitespace + "\"" + match.alias + "\"";
        } else {
            return "AS" + match.whitespace + match.alias;
        }
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

    private static class AliasMatch {
        final int start;
        final int end;
        final String whitespace;
        final String alias;
        final boolean hasQuotes;

        AliasMatch(int start, int end, String whitespace, String alias, boolean hasQuotes) {
            this.start = start;
            this.end = end;
            this.whitespace = whitespace;
            this.alias = alias;
            this.hasQuotes = hasQuotes;
        }
    }
}
