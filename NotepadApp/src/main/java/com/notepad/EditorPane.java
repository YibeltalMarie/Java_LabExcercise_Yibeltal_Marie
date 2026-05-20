package com.notepad;

import javafx.scene.control.TextArea;

/**
 * EditorPane wraps the main TextArea used for editing text.
 * Responsible for: word wrap state, text access, and basic editor settings.
 */
public class EditorPane extends TextArea {

    public EditorPane() {
        setWrapText(true);
        setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 13px;");
    }

    /**
     * Toggle word wrap on or off.
     */
    public void setWordWrap(boolean wrap) {
        setWrapText(wrap);
    }

    /**
     * Replace all content (used by Find & Replace All).
     */
    public void replaceAllText(String newText) {
        setText(newText);
    }
}