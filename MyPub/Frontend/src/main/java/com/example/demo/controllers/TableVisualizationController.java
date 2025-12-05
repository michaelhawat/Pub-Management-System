package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.CustomerApi;
import com.example.demo.api.ReservationApi;
import com.example.demo.api.TableApi;
import com.example.demo.model.CustomerDto;
import com.example.demo.model.ReservationDto;
import com.example.demo.model.TableDto;
import com.example.demo.util.AlertUtils;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.AmbientLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableVisualizationController {

    @FXML private BorderPane rootPane;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> statusFilterCombo;

    private Group root3D;
    private PerspectiveCamera camera;
    private Box platform;
    private SubScene subScene;
    private final Map<Group, TableDto> tableGroupMap = new HashMap<>();
    private final List<Group> tableGroups = new ArrayList<>();

    // Camera movement
    private double cameraDistance = 700;
    private double cameraRotationX = -25;
    private double cameraRotationY = 0;

    // World rotation (NEW)
    private Rotate worldRotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate worldRotateY = new Rotate(0, Rotate.Y_AXIS);

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            try {
                setupStatusFilter();
                setup3DScene();
                loadTables();
            } catch (Exception ex) {
                handleError("Failed to initialize 3D table view", ex);
            }
        });
    }

    private void setupStatusFilter() {
        statusFilterCombo.getItems().addAll("ALL", "AVAILABLE", "OCCUPIED", "RESERVED");
        statusFilterCombo.setValue("ALL");
        statusFilterCombo.setOnAction(e -> loadTables());
    }

    @FXML
    private void refreshData() {
        loadTables();
    }

    @FXML
    private void resetView() {
        cameraDistance = 700;
        cameraRotationX = -25;
        cameraRotationY = 0;

        worldRotateX.setAngle(0);
        worldRotateY.setAngle(0);

        updateCameraPosition();
    }

    @FXML
    private void backToTables() {
        Launcher.go("reservations.fxml", "Reservation Management");
    }

    private void setup3DScene() {
        root3D = new Group();

        // Attach world rotation transforms
        root3D.getTransforms().addAll(worldRotateX, worldRotateY);

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10_000);
        camera.setFieldOfView(50);
        updateCameraPosition();

        AmbientLight ambient = new AmbientLight(Color.color(1, 1, 1, 0.5));
        PointLight keyLight = new PointLight(Color.color(1, 1, 0.95, 0.8));
        keyLight.setTranslateX(-500);
        keyLight.setTranslateY(-400);
        keyLight.setTranslateZ(-500);
        PointLight fillLight = new PointLight(Color.color(0.7, 0.8, 1, 0.6));
        fillLight.setTranslateX(500);
        fillLight.setTranslateY(300);
        fillLight.setTranslateZ(500);
        root3D.getChildren().addAll(ambient, keyLight, fillLight);

        subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(18, 20, 28));
        subScene.setCamera(camera);

        attachMouseControls(subScene);

        StackPane canvas = new StackPane();
        canvas.getStyleClass().add("visualization-canvas");
        canvas.getChildren().add(subScene);
        subScene.widthProperty().bind(canvas.widthProperty());
        subScene.heightProperty().bind(canvas.heightProperty());

        rootPane.setCenter(canvas);
    }

    private void loadTables() {
        if (root3D == null) {
            return;
        }
        updateStatus("Loading tables...");
        try {
            List<TableDto> allTables = TableApi.getAllTables();
            String filter = statusFilterCombo.getValue();

            List<TableDto> filteredTables = new ArrayList<>();
            if ("ALL".equals(filter)) {
                filteredTables.addAll(allTables);
            } else {
                for (TableDto table : allTables) {
                    if (filter.equals(table.getStatus())) {
                        filteredTables.add(table);
                    }
                }
            }

            clearTableGroups();

            if (filteredTables.isEmpty()) {
                updateStatus("No tables found matching filter.");
                return;
            }

            visualizeTables(filteredTables);
            updateStatus("Displaying " + filteredTables.size() + " tables. Click a table to reserve!");
        } catch (Exception ex) {
            handleError("Failed to load tables", ex);
        }
    }

    private void visualizeTables(List<TableDto> tables) {
        int count = tables.size();
        int cols = (int) Math.ceil(Math.sqrt(count));
        int rows = (int) Math.ceil((double) count / cols);

        double spacing = Math.max(180, Math.min(250, 2000d / Math.max(cols, rows)));
        double startX = -(cols - 1) * spacing / 2;
        double startZ = -(rows - 1) * spacing / 2;

        createBasePlatform(cols, rows, spacing, startX, startZ);

        for (int i = 0; i < count; i++) {
            TableDto table = tables.get(i);
            int col = i % cols;
            int row = i / cols;
            double x = startX + col * spacing;
            double z = startZ + row * spacing;

            Group tableGroup = createTable3D(table, i, x, z);
            tableGroups.add(tableGroup);
            tableGroupMap.put(tableGroup, table);
            root3D.getChildren().add(tableGroup);
        }
    }

    private Group createTable3D(TableDto table, int index, double x, double z) {
        Group group = new Group();
        group.setId("table-" + table.getTableId());

        String status = table.getStatus();
        Color tableColor = getStatusColor(status);
        Color glowColor = getStatusGlowColor(status);

        double tableSize = 60 + (table.getCapacity() * 4);
        double tableHeight = 8;
        double legHeight = 45;

        Box tableTop = new Box(tableSize, tableHeight, tableSize);
        PhongMaterial topMaterial = new PhongMaterial(tableColor);
        topMaterial.setSpecularColor(glowColor);
        topMaterial.setSpecularPower(64);
        tableTop.setMaterial(topMaterial);
        tableTop.setTranslateY(-legHeight - tableHeight / 2);
        tableTop.setTranslateX(x);
        tableTop.setTranslateZ(z);

        Cylinder leg1 = createTableLeg(x - tableSize / 3, z - tableSize / 3, -legHeight / 2, tableColor);
        Cylinder leg2 = createTableLeg(x + tableSize / 3, z - tableSize / 3, -legHeight / 2, tableColor);
        Cylinder leg3 = createTableLeg(x - tableSize / 3, z + tableSize / 3, -legHeight / 2, tableColor);
        Cylinder leg4 = createTableLeg(x + tableSize / 3, z + tableSize / 3, -legHeight / 2, tableColor);

        group.getChildren().addAll(tableTop, leg1, leg2, leg3, leg4);

        Box base = new Box(tableSize + 20, 6, tableSize + 20);
        base.setMaterial(new PhongMaterial(Color.color(0.25, 0.25, 0.28)));
        base.setTranslateX(x);
        base.setTranslateY(3);
        base.setTranslateZ(z);
        group.getChildren().add(base);

        Group labelGroup = createTableLabel(table, tableSize, x, z);
        group.getChildren().add(labelGroup);

        addHoverEffect(tableTop, tableColor, glowColor);

        return group;
    }

    private Cylinder createTableLeg(double x, double z, double y, Color color) {
        Cylinder leg = new Cylinder(3, 45);
        PhongMaterial legMaterial = new PhongMaterial(color.darker());
        leg.setMaterial(legMaterial);
        leg.setTranslateX(x);
        leg.setTranslateY(y);
        leg.setTranslateZ(z);
        return leg;
    }

    private Group createTableLabel(TableDto table, double tableSize, double x, double z) {
        String labelText = "Table #" + table.getTableId() + "\n" +
                table.getCapacity() + " seats\n" +
                table.getStatus();

        Text text = new Text(labelText);
        text.setFill(Color.WHITE);
        text.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        text.setWrappingWidth(100);

        double bgWidth = 110;
        double bgHeight = 50;

        javafx.scene.shape.Rectangle bg = new javafx.scene.shape.Rectangle(bgWidth, bgHeight);
        bg.setArcWidth(12);
        bg.setArcHeight(12);
        bg.setFill(Color.color(0, 0, 0, 0.75));
        bg.setStroke(Color.color(1, 1, 1, 0.4));
        bg.setStrokeWidth(1.5);
        bg.setTranslateX(-bgWidth / 2);
        bg.setTranslateY(-bgHeight / 2);

        text.setTranslateX(-bgWidth / 2 + 5);
        text.setTranslateY(-bgHeight / 2 + 15);

        Group labelGroup = new Group(bg, text);
        labelGroup.setTranslateX(x);
        labelGroup.setTranslateY(-tableSize - 60);
        labelGroup.setTranslateZ(z);

        return labelGroup;
    }

    private void addHoverEffect(Box tableTop, Color baseColor, Color glowColor) {
        tableTop.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), tableTop);
            st.setToX(1.1);
            st.setToY(1.1);
            st.setToZ(1.1);
            st.play();

            PhongMaterial mat = (PhongMaterial) tableTop.getMaterial();
            mat.setDiffuseColor(glowColor);
        });

        tableTop.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), tableTop);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setToZ(1.0);
            st.play();

            PhongMaterial mat = (PhongMaterial) tableTop.getMaterial();
            mat.setDiffuseColor(baseColor);
        });
    }

    private Color getStatusColor(String status) {
        return switch (status) {
            case "AVAILABLE" -> Color.color(0.2, 0.7, 0.3);
            case "OCCUPIED" -> Color.color(0.9, 0.3, 0.2);
            case "RESERVED" -> Color.color(0.95, 0.75, 0.1);
            default -> Color.GRAY;
        };
    }

    private Color getStatusGlowColor(String status) {
        return switch (status) {
            case "AVAILABLE" -> Color.color(0.4, 0.9, 0.5);
            case "OCCUPIED" -> Color.color(1.0, 0.5, 0.4);
            case "RESERVED" -> Color.color(1.0, 0.85, 0.3);
            default -> Color.WHITE;
        };
    }

    private void createBasePlatform(int cols, int rows, double spacing, double startX, double startZ) {
        if (platform != null) {
            root3D.getChildren().remove(platform);
        }
        double platformWidth = cols * spacing + 150;
        double platformDepth = rows * spacing + 150;
        platform = new Box(platformWidth, 8, platformDepth);
        PhongMaterial material = new PhongMaterial(Color.color(0.15, 0.17, 0.22));
        platform.setMaterial(material);

        platform.setTranslateX(startX + (cols - 1) * spacing / 2);
        platform.setTranslateY(4);
        platform.setTranslateZ(startZ + (rows - 1) * spacing / 2);

        root3D.getChildren().add(platform);
    }

    private void clearTableGroups() {
        for (Group group : tableGroups) {
            root3D.getChildren().remove(group);
        }
        tableGroups.clear();
        tableGroupMap.clear();
    }

    private void attachMouseControls(SubScene scene) {
        scene.setOnMousePressed((MouseEvent event) -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
        });

        scene.setOnMouseDragged((MouseEvent event) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();

            double deltaX = mousePosX - mouseOldX;
            double deltaY = mousePosY - mouseOldY;

            // Rotate world
            worldRotateY.setAngle(worldRotateY.getAngle() + deltaX * 0.3);
            worldRotateX.setAngle(worldRotateX.getAngle() + deltaY * 0.3);

            // Rotate camera
            cameraRotationY += deltaX * 0.3;
            cameraRotationX += deltaY * 0.3;

            updateCameraPosition();
        });

        // Zoom
        scene.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = 1.0 + event.getDeltaY() * 0.001;
            cameraDistance = Math.max(200, Math.min(2000, cameraDistance * zoomFactor));
            updateCameraPosition();
            event.consume();
        });

        // Click detection
        scene.setOnMouseClicked((MouseEvent event) -> {
            PickResult pickResult = event.getPickResult();
            if (pickResult != null && pickResult.getIntersectedNode() != null) {
                javafx.scene.Node node = pickResult.getIntersectedNode();
                Group tableGroup = findTableGroup(node);
                if (tableGroup != null) {
                    TableDto table = tableGroupMap.get(tableGroup);
                    if (table != null) {
                        handleTableClick(table);
                    }
                }
            }
        });
    }

    private void updateCameraPosition() {
        Rotate rotateX = new Rotate(cameraRotationX, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(cameraRotationY, Rotate.Y_AXIS);

        camera.getTransforms().clear();
        camera.getTransforms().addAll(
                new Translate(0, -150, -cameraDistance),
                rotateY,
                rotateX
        );
    }

    private Group findTableGroup(javafx.scene.Node node) {
        javafx.scene.Node current = node;
        while (current != null) {
            if (current instanceof Group && current.getId() != null && current.getId().startsWith("table-")) {
                return (Group) current;
            }
            current = current.getParent();
        }
        return null;
    }

    private void handleTableClick(TableDto table) {
        if ("OCCUPIED".equals(table.getStatus())) {
            AlertUtils.warn("This table is currently occupied. Please select an available table.");
            return;
        }

        showReservationDialog(table);
    }

    private void showReservationDialog(TableDto table) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/demo/fxml/reservation-dialog.fxml"));
            VBox dialogRoot = loader.load();
            ReservationDialogController controller = loader.getController();
            controller.setTable(table);
            controller.setParentController(this);

            Stage dialog = new Stage();
            dialog.setTitle("Reserve Table #" + table.getTableId());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(rootPane.getScene().getWindow());
            dialog.setScene(new javafx.scene.Scene(dialogRoot, 500, 450));
            dialog.getScene().getStylesheets().add(
                    getClass().getResource("/com/example/demo/styles.css").toExternalForm());
            dialog.showAndWait();
        } catch (Exception ex) {
            handleError("Failed to open reservation dialog", ex);
        }
    }

    public void onReservationCreated() {
        loadTables();
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void handleError(String message, Exception ex) {
        updateStatus(message + ": " + ex.getMessage());
        AlertUtils.error(message + ": " + ex.getMessage());
        ex.printStackTrace();
    }
}
