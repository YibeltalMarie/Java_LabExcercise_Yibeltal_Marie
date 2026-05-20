package com.notepad;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * MenuBarBuilder constructs the full application MenuBar.
 *
 * Menus built here:
 *   File   — New, Open, Save, Save As, Exit
 *   Edit   — Cut, Copy, Paste, Select All, Find, Find & Replace
 *   Format — Word Wrap, Font
 *   Help   — About
 *
 * All keyboard shortcuts are set here. Actions are delegated to
 * FileManager, FindReplaceDialog, and FontChooserDialog.
 */
public class MenuBarBuilder {

    private final Stage             owner;
    private final EditorPane        editor;
    private final FileManager       fileManager;
    private final FindReplaceDialog findReplace;
    private final FontChooserDialog fontChooser;
    private final Runnable          onExit;

    public MenuBarBuilder(
            Stage             owner,
            EditorPane        editor,
            FileManager       fileManager,
            FindReplaceDialog findReplace,
            FontChooserDialog fontChooser,
            Runnable          onExit) {
        this.owner       = owner;
        this.editor      = editor;
        this.fileManager = fileManager;
        this.findReplace = findReplace;
        this.fontChooser = fontChooser;
        this.onExit      = onExit;
    }

    /** Build and return the complete MenuBar. */
    public MenuBar build() {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(
            buildFileMenu(),
            buildEditMenu(),
            buildFormatMenu(),
            buildHelpMenu()
        );
        return menuBar;
    }

    // ── File Menu ─────────────────────────────────────────────

    private Menu buildFileMenu() {
        MenuItem newItem    = new MenuItem("New");
        MenuItem openItem   = new MenuItem("Open...");
        MenuItem saveItem   = new MenuItem("Save");
        MenuItem saveAsItem = new MenuItem("Save As...");
        MenuItem exitItem   = new MenuItem("Exit");

        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveAsItem.setAccelerator(new KeyCodeCombination(KeyCode.S,
                KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

        newItem.setOnAction(e    -> fileManager.newFile());
        openItem.setOnAction(e   -> fileManager.openFile());
        saveItem.setOnAction(e   -> fileManager.saveFile());
        saveAsItem.setOnAction(e -> fileManager.saveFileAs());
        exitItem.setOnAction(e   -> onExit.run());

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
            newItem, openItem,
            new SeparatorMenuItem(),
            saveItem, saveAsItem,
            new SeparatorMenuItem(),
            exitItem
        );
        return fileMenu;
    }

    // ── Edit Menu ─────────────────────────────────────────────

    private Menu buildEditMenu() {
        MenuItem cutItem       = new MenuItem("Cut");
        MenuItem copyItem      = new MenuItem("Copy");
        MenuItem pasteItem     = new MenuItem("Paste");
        MenuItem selectAllItem = new MenuItem("Select All");
        MenuItem findItem      = new MenuItem("Find...");
        MenuItem findReplItem  = new MenuItem("Find & Replace...");

        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        selectAllItem.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
        findItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        findReplItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));

        cutItem.setOnAction(e       -> editor.cut());
        copyItem.setOnAction(e      -> editor.copy());
        pasteItem.setOnAction(e     -> editor.paste());
        selectAllItem.setOnAction(e -> editor.selectAll());
        findItem.setOnAction(e      -> findReplace.show());
        findReplItem.setOnAction(e  -> findReplace.show());

        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(
            cutItem, copyItem, pasteItem,
            new SeparatorMenuItem(),
            selectAllItem,
            new SeparatorMenuItem(),
            findItem, findReplItem
        );
        return editMenu;
    }

    // ── Format Menu ───────────────────────────────────────────

    private Menu buildFormatMenu() {
        CheckMenuItem wordWrapItem = new CheckMenuItem("Word Wrap");
        wordWrapItem.setSelected(true); // on by default
        wordWrapItem.setOnAction(e -> editor.setWordWrap(wordWrapItem.isSelected()));

        MenuItem fontItem = new MenuItem("Font...");
        fontItem.setOnAction(e -> fontChooser.show());

        Menu formatMenu = new Menu("Format");
        formatMenu.getItems().addAll(wordWrapItem, new SeparatorMenuItem(), fontItem);
        return formatMenu;
    }

    // ── Help Menu ─────────────────────────────────────────────

    private Menu buildHelpMenu() {
        MenuItem aboutItem = new MenuItem("About Notepad");
        aboutItem.setOnAction(e -> {
            Alert about = new Alert(Alert.AlertType.INFORMATION);
            about.initOwner(owner);
            about.setTitle("About Notepad");
            about.setHeaderText("JavaFX Notepad");
            about.setContentText("""
                A Notepad clone built with JavaFX 21.
                
                Features:
                  • Open, save, and edit text files
                  • Find & Replace with match-case
                  • Custom font chooser
                  • Word wrap toggle
                  • Live status bar (line, column, characters)
                  • Full keyboard shortcuts
                """);
            about.showAndWait();
        });

        Menu helpMenu = new Menu("Help");
        helpMenu.getItems().add(aboutItem);
        return helpMenu;
    }
}