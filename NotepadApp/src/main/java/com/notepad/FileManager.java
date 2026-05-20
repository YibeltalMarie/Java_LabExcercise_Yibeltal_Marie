package com.notepad;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * FileManager handles all file operations:
 * new file, open, save, save as, and unsaved-changes confirmation.
 *
 * It does NOT touch the UI layout — it only reads/writes the EditorPane
 * and updates the window title via a callback.
 */
public class FileManager {

    private final Stage        owner;
    private final EditorPane   editor;
    private final Runnable     onTitleChange;

    private File    currentFile = null;
    private boolean isModified  = false;

    public FileManager(Stage owner, EditorPane editor, Runnable onTitleChange) {
        this.owner         = owner;
        this.editor        = editor;
        this.onTitleChange = onTitleChange;

        // Mark as modified on every keystroke
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            isModified = true;
            onTitleChange.run();
        });
    }

    // ── Public API ────────────────────────────────────────────

    public void newFile() {
        if (!confirmDiscard()) return;
        editor.clear();
        currentFile = null;
        isModified  = false;
        onTitleChange.run();
    }

    public void openFile() {
        if (!confirmDiscard()) return;
        File file = showOpenDialog();
        if (file == null) return;
        try {
            editor.setText(Files.readString(file.toPath()));
            currentFile = file;
            isModified  = false;
            onTitleChange.run();
        } catch (IOException ex) {
            showError("Could not open file:\n" + ex.getMessage());
        }
    }

    public boolean saveFile() {
        if (currentFile == null) return saveFileAs();
        return writeToFile(currentFile);
    }

    public boolean saveFileAs() {
        File file = showSaveDialog();
        if (file == null) return false;
        currentFile = file;
        return writeToFile(file);
    }

    /** Returns true if the caller may proceed (no unsaved changes, or user chose Save/Discard). */
    public boolean confirmDiscard() {
        if (!isModified) return true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes.");
        alert.setContentText("Do you want to save before continuing?");

        ButtonType saveBtn    = new ButtonType("Save");
        ButtonType discardBtn = new ButtonType("Discard");
        ButtonType cancelBtn  = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(saveBtn, discardBtn, cancelBtn);

        ButtonType result = alert.showAndWait().orElse(cancelBtn);
        if (result == saveBtn)    return saveFile();
        if (result == discardBtn) return true;
        return false;
    }

    // ── Title helpers (used by NotepadApp) ───────────────────

    public String getTitle() {
        String name = (currentFile != null) ? currentFile.getName() : "Untitled";
        return (isModified ? "*" : "") + name + " - Notepad";
    }

    // ── Private helpers ───────────────────────────────────────

    private boolean writeToFile(File file) {
        try {
            Files.writeString(file.toPath(), editor.getText());
            isModified = false;
            onTitleChange.run();
            return true;
        } catch (IOException ex) {
            showError("Could not save file:\n" + ex.getMessage());
            return false;
        }
    }

    private File showOpenDialog() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open File");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.java", "*.xml", "*.csv"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return fc.showOpenDialog(owner);
    }

    private File showSaveDialog() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save As");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        return fc.showSaveDialog(owner);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.initOwner(owner);
        alert.setTitle("Error");
        alert.showAndWait();
    }
}