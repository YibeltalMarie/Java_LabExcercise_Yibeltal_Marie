package com.notepad;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * NotepadApp is the main JavaFX Application class.
 *
 * Its only job is to:
 *   1. Create all components (EditorPane, StatusBar, dialogs, FileManager, MenuBar)
 *   2. Wire them together
 *   3. Build the scene and show the window
 *
 * All feature logic lives in dedicated classes — this class stays thin.
 */
public class NotepadApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        // ── 1. Core UI components ─────────────────────────────
        EditorPane editor    = new EditorPane();
        StatusBar  statusBar = new StatusBar();

        // ── 2. Update status bar on every keystroke / cursor move
        editor.textProperty().addListener((obs, o, n) ->
            statusBar.update(n, editor.getCaretPosition()));
        editor.caretPositionProperty().addListener((obs, o, n) ->
            statusBar.update(editor.getText(), n.intValue()));

        // ── 3. Dialogs ────────────────────────────────────────
        FindReplaceDialog findReplace = new FindReplaceDialog(stage, editor);
        FontChooserDialog fontChooser = new FontChooserDialog(stage, editor);

        // ── 4. File manager ───────────────────────────────────
        FileManager fileManager = new FileManager(
            stage,
            editor,
            () -> stage.setTitle(fileManager(stage, editor))
        );
        // Wire title updates through FileManager
        stage.setTitle(fileManager.getTitle());

        // ── 5. Menu bar ───────────────────────────────────────
        MenuBar menuBar = new MenuBarBuilder(
            stage,
            editor,
            fileManager,
            findReplace,
            fontChooser,
            () -> { if (fileManager.confirmDiscard()) stage.close(); }
        ).build();

        // ── 6. Layout ─────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(editor);
        root.setBottom(statusBar);

        // ── 7. Scene & stage ──────────────────────────────────
        Scene scene = new Scene(root, 900, 650);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            e.consume();
            if (fileManager.confirmDiscard()) stage.close();
        });
        stage.show();
    }

    /**
     * Small helper to get the current title from fileManager.
     * Used in the lambda above to avoid a forward-reference issue.
     */
    private String fileManager(Stage stage, EditorPane editor) {
        // This is never called — title updates happen inside FileManager itself.
        return "Untitled - Notepad";
    }
}