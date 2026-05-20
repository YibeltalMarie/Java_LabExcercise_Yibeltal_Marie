package com.notepad;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * FontChooserDialog lets the user pick a font family, style, and size
 * for the editor, exactly like the real Notepad's Format → Font dialog.
 */
public class FontChooserDialog {

    private final Stage      owner;
    private final EditorPane editor;

    public FontChooserDialog(Stage owner, EditorPane editor) {
        this.owner  = owner;
        this.editor = editor;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.setTitle("Font");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setResizable(false);

        // ── Font families ────────────────────────────────────
        List<String> families = Font.getFamilies();
        ListView<String> familyList = new ListView<>(FXCollections.observableArrayList(families));
        familyList.setPrefSize(200, 180);

        // ── Font styles ──────────────────────────────────────
        ListView<String> styleList = new ListView<>(
            FXCollections.observableArrayList("Regular", "Bold", "Italic", "Bold Italic")
        );
        styleList.setPrefSize(130, 180);

        // ── Font sizes ───────────────────────────────────────
        List<String> sizes = List.of(
            "8","9","10","11","12","14","16","18","20","22","24","26","28","36","48","72"
        );
        ListView<String> sizeList = new ListView<>(FXCollections.observableArrayList(sizes));
        sizeList.setPrefSize(70, 180);

        // ── Text fields (editable header above each list) ────
        TextField familyField = new TextField();
        TextField styleField  = new TextField();
        TextField sizeField   = new TextField("12");

        familyField.setPrefWidth(200);
        styleField.setPrefWidth(130);
        sizeField.setPrefWidth(70);

        // Sync list → field
        familyList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) { familyField.setText(n); updatePreview(familyField, styleField, sizeField, previewLabel()); }
        });
        styleList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) { styleField.setText(n); updatePreview(familyField, styleField, sizeField, previewLabel()); }
        });
        sizeList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) { sizeField.setText(n); updatePreview(familyField, styleField, sizeField, previewLabel()); }
        });

        // ── Pre-select current editor font ───────────────────
        String currentStyle  = editor.getStyle();
        String currentFamily = extractStyleValue(currentStyle, "-fx-font-family", "Monospaced").replace("'", "");
        String currentSize   = extractStyleValue(currentStyle, "-fx-font-size", "13px").replace("px", "");

        familyField.setText(currentFamily);
        styleField.setText("Regular");
        sizeField.setText(currentSize);
        familyList.getSelectionModel().select(currentFamily);
        sizeList.getSelectionModel().select(currentSize);

        // ── Preview label ─────────────────────────────────────
        Label preview = new Label("AaBbYyZz 012");
        preview.setStyle(buildFontStyle(currentFamily, "Regular", currentSize));
        preview.setPrefHeight(60);

        // Store reference so listeners can update it
        preview.setId("preview");

        // ── Layout ───────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Font:"),       0, 0);
        grid.add(new Label("Font style:"), 1, 0);
        grid.add(new Label("Size:"),       2, 0);
        grid.add(familyField,              0, 1);
        grid.add(styleField,               1, 1);
        grid.add(sizeField,                2, 1);
        grid.add(familyList,               0, 2);
        grid.add(styleList,                1, 2);
        grid.add(sizeList,                 2, 2);

        // Preview area
        TitledPane previewPane = new TitledPane("Sample", preview);
        previewPane.setCollapsible(false);
        previewPane.setPadding(new Insets(0, 16, 0, 16));

        // Buttons
        Button okBtn     = new Button("OK");
        Button cancelBtn = new Button("Cancel");
        okBtn.setStyle("""
            -fx-background-color: #0078d7;
            -fx-text-fill: white;
            -fx-min-width: 80px;
            -fx-padding: 6 16 6 16;
        """);
        cancelBtn.setStyle("-fx-min-width: 80px; -fx-padding: 6 16 6 16;");

        HBox btnBox = new HBox(10, okBtn, cancelBtn);
        btnBox.setPadding(new Insets(10, 16, 16, 16));

        // Update preview when fields are typed in directly
        familyField.textProperty().addListener((obs, o, n) ->
            updatePreviewDirect(familyField, styleField, sizeField, preview));
        styleField.textProperty().addListener((obs, o, n) ->
            updatePreviewDirect(familyField, styleField, sizeField, preview));
        sizeField.textProperty().addListener((obs, o, n) ->
            updatePreviewDirect(familyField, styleField, sizeField, preview));

        okBtn.setOnAction(e -> {
            applyFont(familyField.getText(), styleField.getText(), sizeField.getText());
            dialog.close();
        });
        cancelBtn.setOnAction(e -> dialog.close());

        VBox root = new VBox(grid, previewPane, btnBox);
        root.setStyle("-fx-background-color: #fafafa;");

        dialog.setScene(new Scene(root, 450, 420));
        dialog.show();
    }

    // ── Helpers ───────────────────────────────────────────────

    /** Fallback — returns a dummy label (only used as a placeholder before preview is wired). */
    private Label previewLabel() { return new Label(); }

    private void updatePreview(TextField family, TextField style, TextField size, Label label) {
        label.setStyle(buildFontStyle(family.getText(), style.getText(), size.getText()));
    }

    private void updatePreviewDirect(TextField family, TextField style, TextField size, Label preview) {
        preview.setStyle(buildFontStyle(family.getText(), style.getText(), size.getText()));
    }

    private void applyFont(String family, String style, String sizeStr) {
        editor.setStyle(buildFontStyle(family, style, sizeStr));
    }

    private String buildFontStyle(String family, String style, String sizeStr) {
        String weight  = style.toLowerCase().contains("bold")   ? "bold"   : "normal";
        String posture = style.toLowerCase().contains("italic") ? "italic" : "normal";
        double size;
        try { size = Double.parseDouble(sizeStr.replace("px", "")); }
        catch (NumberFormatException e) { size = 13; }
        return String.format(
            "-fx-font-family: '%s'; -fx-font-size: %.0fpx; -fx-font-weight: %s; -fx-font-style: %s;",
            family, size, weight, posture
        );
    }

    private String extractStyleValue(String style, String key, String defaultVal) {
        if (style == null || !style.contains(key)) return defaultVal;
        int start = style.indexOf(key) + key.length() + 1;
        int end   = style.indexOf(";", start);
        return (end > start) ? style.substring(start, end).trim() : defaultVal;
    }
}