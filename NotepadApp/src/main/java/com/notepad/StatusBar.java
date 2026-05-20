package com.notepad;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;

/**
 * StatusBar displays live editor info at the bottom of the window:
 * current line number, column position, and total character count.
 */
public class StatusBar extends HBox {

    private final Label lineColLabel;
    private final Label charCountLabel;

    public StatusBar() {
        lineColLabel  = new Label("Ln 1, Col 1");
        charCountLabel = new Label("Characters: 0");

        lineColLabel.setStyle("-fx-font-size: 12px; -fx-padding: 0 16 0 8;");
        charCountLabel.setStyle("-fx-font-size: 12px; -fx-padding: 0 8 0 0;");

        Separator sep = new Separator(Orientation.VERTICAL);

        getChildren().addAll(lineColLabel, sep, charCountLabel);
        setPadding(new Insets(4, 8, 4, 8));
        setSpacing(10);
        setStyle("""
            -fx-background-color: #f0f0f0;
            -fx-border-color: #cccccc;
            -fx-border-width: 1 0 0 0;
        """);
    }

    /**
     * Call this whenever the caret moves or text changes.
     * Recalculates line, column, and character count from the editor.
     */
    public void update(String fullText, int caretPosition) {
        String textBeforeCaret = fullText.substring(0, Math.min(caretPosition, fullText.length()));
        String[] lines = textBeforeCaret.split("\n", -1);

        int currentLine = lines.length;
        int currentCol  = lines[currentLine - 1].length() + 1;

        lineColLabel.setText("Ln " + currentLine + ", Col " + currentCol);
        charCountLabel.setText("Characters: " + fullText.length());
    }
}