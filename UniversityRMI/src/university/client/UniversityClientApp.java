package university.client;

import university.common.UniversityService;
import university.common.StudentDTO;
import university.common.TeacherDTO;
import university.server.ServerMain;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Responsibility: JavaFX GUI that communicates exclusively via RMI.
 * No SQL, no DAO, no direct DB access — only calls UniversityService.
 */
public class UniversityClientApp extends Application {

    private UniversityService service;
    private Label connectionStatus;

    // ── RMI connection ───────────────────────────────────────────────────────

    private boolean connect() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", ServerMain.PORT);
            service = (UniversityService) registry.lookup(ServerMain.SERVICE_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Application entry ────────────────────────────────────────────────────

    @Override
    public void start(Stage stage) {
        stage.setTitle("University System — RMI Client");

        boolean connected = connect();

        connectionStatus = new Label(connected
            ? "● Connected to RMI Server (localhost:" + ServerMain.PORT + ")"
            : "● Not connected — start ServerMain first");
        connectionStatus.setStyle(connected
            ? "-fx-text-fill: green; -fx-font-weight: bold;"
            : "-fx-text-fill: red;  -fx-font-weight: bold;");
        connectionStatus.setPadding(new Insets(6, 12, 6, 12));

        Button retryBtn = new Button("Retry");
        retryBtn.setOnAction(e -> {
            if (connect()) {
                connectionStatus.setText("● Connected to RMI Server (localhost:" + ServerMain.PORT + ")");
                connectionStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            }
        });

        HBox statusBar = new HBox(10, connectionStatus, retryBtn);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(4, 10, 4, 10));
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 0 0 1 0;");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
            buildStudentTab(),
            buildShowStudentsTab(),
            buildTeacherTab(),
            buildShowTeachersTab()
        );

        BorderPane root = new BorderPane();
        root.setTop(statusBar);
        root.setCenter(tabPane);

        stage.setScene(new Scene(root, 560, 500));
        stage.show();
    }

    // ── Student — Add tab ────────────────────────────────────────────────────

    private Tab buildStudentTab() {
        Tab tab = new Tab("Add Student");

        TextField tfId   = field("ID (number)");
        TextField tfName = field("Full name");
        TextField tfDept = field("Department");
        TextField tfSec  = field("Section");
        TextField tfYear = field("Year");
        Label     status = new Label();
        Button    btn    = new Button("Add Student");

        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            try {
                if (service == null) { setError(status, "Not connected"); return; }
                StudentDTO s = new StudentDTO(
                    Integer.parseInt(tfId.getText().trim()),
                    tfName.getText().trim(),
                    tfDept.getText().trim(),
                    tfSec.getText().trim(),
                    Integer.parseInt(tfYear.getText().trim())
                );
                service.addStudent(s);
                status.setText("✓ Student \"" + s.getName() + "\" added!");
                status.setStyle("-fx-text-fill: green;");
                tfId.clear(); tfName.clear(); tfDept.clear(); tfSec.clear(); tfYear.clear();
            } catch (NumberFormatException ex) {
                setError(status, "ID and Year must be numbers.");
            } catch (Exception ex) {
                setError(status, ex.getMessage());
            }
        });

        // FIX: ScrollPane so all 5 fields + button are always visible/reachable
        tab.setContent(scrollableForm(status, btn,
            "ID:", tfId, "Name:", tfName,
            "Department:", tfDept, "Section:", tfSec, "Year:", tfYear));
        return tab;
    }

    // ── Student — Show tab ───────────────────────────────────────────────────

    private Tab buildShowStudentsTab() {
        Tab tab = new Tab("Show Students");

        TableView<StudentDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // FIX: lambda cell factories — directly call getters on the DTO.
        // PropertyValueFactory uses reflection and silently returns empty cells
        // when the module system blocks access; lambdas always work.
        TableColumn<StudentDTO, Integer> colId   = new TableColumn<>("ID");
        TableColumn<StudentDTO, String>  colName = new TableColumn<>("Name");
        TableColumn<StudentDTO, String>  colDept = new TableColumn<>("Department");
        TableColumn<StudentDTO, String>  colSec  = new TableColumn<>("Section");
        TableColumn<StudentDTO, Integer> colYear = new TableColumn<>("Year");

        colId  .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colDept.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDepartment()));
        colSec .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSection()));
        colYear.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getYear()).asObject());

        colId.setPrefWidth(50); colName.setPrefWidth(140); colDept.setPrefWidth(110);
        colSec.setPrefWidth(70); colYear.setPrefWidth(60);

        table.getColumns().addAll(colId, colName, colDept, colSec, colYear);

        Label status = new Label();
        Button btn   = new Button("Refresh");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            try {
                if (service == null) { setError(status, "Not connected"); return; }
                List<StudentDTO> list = service.getStudents();
                table.setItems(FXCollections.observableArrayList(list));
                status.setText("✓ " + list.size() + " student(s) loaded.");
                status.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) {
                setError(status, ex.getMessage());
            }
        });

        VBox box = new VBox(8, btn, table, status);
        box.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(box);
        return tab;
    }

    // ── Teacher — Add tab ────────────────────────────────────────────────────

    private Tab buildTeacherTab() {
        Tab tab = new Tab("Add Teacher");

        TextField tfId   = field("ID (number)");
        TextField tfName = field("Full name");
        TextField tfDept = field("Department");
        Label     status = new Label();
        Button    btn    = new Button("Add Teacher");

        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            try {
                if (service == null) { setError(status, "Not connected"); return; }
                TeacherDTO t = new TeacherDTO(
                    Integer.parseInt(tfId.getText().trim()),
                    tfName.getText().trim(),
                    tfDept.getText().trim()
                );
                service.addTeacher(t);
                status.setText("✓ Teacher \"" + t.getName() + "\" added!");
                status.setStyle("-fx-text-fill: green;");
                tfId.clear(); tfName.clear(); tfDept.clear();
            } catch (NumberFormatException ex) {
                setError(status, "ID must be a number.");
            } catch (Exception ex) {
                setError(status, ex.getMessage());
            }
        });

        tab.setContent(scrollableForm(status, btn,
            "ID:", tfId, "Name:", tfName, "Department:", tfDept));
        return tab;
    }

    // ── Teacher — Show tab ───────────────────────────────────────────────────

    private Tab buildShowTeachersTab() {
        Tab tab = new Tab("Show Teachers");

        TableView<TeacherDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // FIX: same lambda approach as student table
        TableColumn<TeacherDTO, Integer> colId   = new TableColumn<>("ID");
        TableColumn<TeacherDTO, String>  colName = new TableColumn<>("Name");
        TableColumn<TeacherDTO, String>  colDept = new TableColumn<>("Department");

        colId  .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colDept.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDepartment()));

        colId.setPrefWidth(60); colName.setPrefWidth(200); colDept.setPrefWidth(150);

        table.getColumns().addAll(colId, colName, colDept);

        Label status = new Label();
        Button btn   = new Button("Refresh");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            try {
                if (service == null) { setError(status, "Not connected"); return; }
                List<TeacherDTO> list = service.getTeachers();
                table.setItems(FXCollections.observableArrayList(list));
                status.setText("✓ " + list.size() + " teacher(s) loaded.");
                status.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) {
                setError(status, ex.getMessage());
            }
        });

        VBox box = new VBox(8, btn, table, status);
        box.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(box);
        return tab;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        return tf;
    }

    private void setError(Label lbl, String msg) {
        lbl.setText("✗ " + msg);
        lbl.setStyle("-fx-text-fill: red;");
    }

    /**
     * Wraps a label/field form in a ScrollPane so all fields + button
     * are always reachable regardless of window height.
     */
    private ScrollPane scrollableForm(Label status, Button btn, Object... labelFieldPairs) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(20));
        box.getChildren().add(status);
        for (int i = 0; i < labelFieldPairs.length; i += 2) {
            box.getChildren().addAll(
                new Label((String) labelFieldPairs[i]),
                (TextField) labelFieldPairs[i + 1]
            );
        }
        box.getChildren().add(btn);

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);  // stretch form to full tab width
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    public static void main(String[] args) {
        launch(args);
    }
}