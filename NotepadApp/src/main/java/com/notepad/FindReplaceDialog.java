package com.notepad;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FindReplaceDialog is a floating, non-blocking dialog for finding
 * and replacing text inside the EditorPane.
 *
 * Features:
 *  - Find Next / Find Previous with wrap-around
 *  - Replace single occurrence
 *  - Replace All
 *  - Match case toggle
 *  - Green/red status feedback
 *  - Only one instance open at a time
 */
public class FindReplaceDialog {

    private final Stage      owner;
    private final EditorPane editor;
    private Stage            dialogStage = null;

    public FindReplaceDialog(Stage owner, EditorPane editor) {
        this.owner  = owner;
        this.editor = editor;
    }

    /** Open the dialog (or bring it to front if already open). */
    public void show() {
        if (dialogStage != null && dialogStage.isShowing()) {
            dialogStage.requestFocus();
            return;
        }

        dialogStage = new Stage();
        dialogStage.setTitle("Find & Replace");
        dialogStage.initModality(Modality.NONE);
        dialogStage.initOwner(owner);
        dialogStage.setResizable(false);

        // ── Input fields ─────────────────────────────────────
        TextField findField    = new TextField();
        TextField replaceField = new TextField();
        CheckBox  matchCase    = new CheckBox("Match case");

        findField.setPromptText("Text to find...");
        replaceField.setPromptText("Replace with...");
        findField.setPrefWidth(260);
        replaceField.setPrefWidth(260);

        // ── Buttons ──────────────────────────────────────────
        Button findNextBtn   = new Button("Find Next");
        Button findPrevBtn   = new Button("Find Previous");
        Button replaceBtn    = new Button("Replace");
        Button replaceAllBtn = new Button("Replace All");
        Button closeBtn      = new Button("Close");

        String baseStyle = """
            -fx-min-width: 130px;
            -fx-padding: 6 12 6 12;
            -fx-font-size: 13px;
        """;
        String primaryStyle = baseStyle + """
            -fx-background-color: #0078d7;
            -fx-text-fill: white;
            -fx-font-weight: bold;
        """;

        findNextBtn.setStyle(primaryStyle);
        findPrevBtn.setStyle(baseStyle);
        replaceBtn.setStyle(baseStyle);
        replaceAllBtn.setStyle(baseStyle);
        closeBtn.setStyle(baseStyle);

        // ── Layout ───────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Find:"),    0, 0);
        grid.add(findField,             1, 0);
        grid.add(new Label("Replace:"), 0, 1);
        grid.add(replaceField,          1, 1);
        grid.add(matchCase,             1, 2);

        VBox btnBox = new VBox(8, findNextBtn, findPrevBtn, replaceBtn, replaceAllBtn, closeBtn);
        btnBox.setPadding(new Insets(20, 20, 20, 0));
        btnBox.setAlignment(Pos.TOP_CENTER);

        Label statusLabel = new Label(" ");
        statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px; -fx-padding: 0 20 10 20;");

        BorderPane root = new BorderPane();
        root.setCenter(grid);
        root.setRight(btnBox);
        root.setBottom(statusLabel);
        root.setStyle("-fx-background-color: #fafafa;");

        // ── Search state ─────────────────────────────────────
        final int[] lastIndex = {-1};

        // Reset index whenever the search term changes
        findField.textProperty().addListener((obs, o, n) -> lastIndex[0] = -1);

        // ── Button logic ─────────────────────────────────────
        findNextBtn.setOnAction(e -> {
            String query  = findField.getText();
            if (query.isEmpty()) return;

            String content = resolve(editor.getText(), matchCase.isSelected());
            String search  = resolve(query,            matchCase.isSelected());

            int from = (lastIndex[0] >= 0) ? lastIndex[0] + 1 : editor.getCaretPosition();
            int idx  = content.indexOf(search, from);
            if (idx == -1) idx = content.indexOf(search, 0); // wrap around

            if (idx >= 0) {
                editor.selectRange(idx, idx + search.length());
                lastIndex[0] = idx;
                setStatus(statusLabel, "Found at position " + idx, true);
            } else {
                setStatus(statusLabel, "\"" + query + "\" not found.", false);
                lastIndex[0] = -1;
            }
        });

        findPrevBtn.setOnAction(e -> {
            String query  = findField.getText();
            if (query.isEmpty()) return;

            String content = resolve(editor.getText(), matchCase.isSelected());
            String search  = resolve(query,            matchCase.isSelected());

            int from = (lastIndex[0] > 0) ? lastIndex[0] - 1 : editor.getCaretPosition() - 1;
            int idx  = content.lastIndexOf(search, from);
            if (idx == -1) idx = content.lastIndexOf(search); // wrap around

            if (idx >= 0) {
                editor.selectRange(idx, idx + search.length());
                lastIndex[0] = idx;
                setStatus(statusLabel, "Found at position " + idx, true);
            } else {
                setStatus(statusLabel, "\"" + query + "\" not found.", false);
                lastIndex[0] = -1;
            }
        });

        replaceBtn.setOnAction(e -> {
            String selected = editor.getSelectedText();
            String query    = findField.getText();
            boolean matches = matchCase.isSelected()
                    ? selected.equals(query)
                    : selected.equalsIgnoreCase(query);

            if (!selected.isEmpty() && matches) {
                editor.replaceSelection(replaceField.getText());
                setStatus(statusLabel, "Replaced 1 occurrence.", true);
            } else {
                findNextBtn.fire(); // find first, then user clicks Replace again
            }
        });

        replaceAllBtn.setOnAction(e -> {
            String query       = findField.getText();
            String replacement = replaceField.getText();
            if (query.isEmpty()) return;

            String content = editor.getText();
            String result;
            int count;

            if (matchCase.isSelected()) {
                count  = content.split(java.util.regex.Pattern.quote(query), -1).length - 1;
                result = content.replace(query, replacement);
            } else {
                count  = content.split("(?i)" + java.util.regex.Pattern.quote(query), -1).length - 1;
                result = content.replaceAll(
                    "(?i)" + java.util.regex.Pattern.quote(query),
                    java.util.regex.Matcher.quoteReplacement(replacement)
                );
            }

            if (count > 0) {
                editor.replaceAllText(result);
                setStatus(statusLabel, "Replaced " + count + " occurrence(s).", true);
            } else {
                setStatus(statusLabel, "\"" + query + "\" not found.", false);
            }
            lastIndex[0] = -1;
        });

        closeBtn.setOnAction(e -> dialogStage.close());

        dialogStage.setScene(new Scene(root, 480, 240));
        dialogStage.show();
    }

    // ── Helpers ───────────────────────────────────────────────

    /** If not matching case, lowercase the string; otherwise return as-is. */
    private String resolve(String text, boolean matchCase) {
        return matchCase ? text : text.toLowerCase();
    }

    private void setStatus(Label label, String msg, boolean success) {
        label.setText(msg);
        String color = success ? "#107010" : "#cc0000";
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-padding: 0 20 10 20;");
    }
}