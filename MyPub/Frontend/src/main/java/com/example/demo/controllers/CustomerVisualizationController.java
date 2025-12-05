package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.CustomerApi;
import com.example.demo.model.CustomerDto;
import com.example.demo.util.AlertUtils;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.SceneAntialiasing;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.scene.AmbientLight;
import javafx.scene.PointLight;
import javafx.scene.PerspectiveCamera;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomerVisualizationController {

    @FXML private BorderPane rootPane;
    @FXML private Label statusLabel;

    private final Rotate rotateX = new Rotate(-20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private Group root3D;
    private PerspectiveCamera camera;
    private Box platform;
    private SubScene subScene;
    private final List<Group> customerGroups = new ArrayList<>();

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            try {
                setup3DScene();
                loadCustomers();
            } catch (Exception ex) {
                handleError("Failed to initialize 3D view", ex);
            }
        });
    }

    @FXML
    private void refreshData() {
        loadCustomers();
    }

    @FXML
    private void backToCustomers() {
        Launcher.go("customers.fxml", "Customer Management");
    }

    private void setup3DScene() {
        if (rootPane == null) {
            throw new IllegalStateException("Root pane not injected");
        }

        root3D = new Group();
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10_000);
        camera.setFieldOfView(45);
        camera.getTransforms().addAll(
                new Translate(0, -200, -800),
                rotateX,
                rotateY
        );

        AmbientLight ambient = new AmbientLight(Color.color(1, 1, 1, 0.6));
        PointLight keyLight = new PointLight(Color.color(1, 1, 1, 0.9));
        keyLight.setTranslateX(-400);
        keyLight.setTranslateY(-300);
        keyLight.setTranslateZ(-600);
        PointLight fillLight = new PointLight(Color.LIGHTBLUE);
        fillLight.setTranslateX(400);
        fillLight.setTranslateY(200);
        fillLight.setTranslateZ(600);
        root3D.getChildren().addAll(ambient, keyLight, fillLight);

        subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(16, 19, 32));
        subScene.setCamera(camera);

        attachMouseControls(subScene);

        StackPane canvas = new StackPane();
        canvas.getStyleClass().add("visualization-canvas");
        canvas.getChildren().add(subScene);
        subScene.widthProperty().bind(canvas.widthProperty());
        subScene.heightProperty().bind(canvas.heightProperty());

        rootPane.setCenter(canvas);
    }

    private void loadCustomers() {
        if (root3D == null) {
            return;
        }
        updateStatus("Loading customer data...");
        try {
            List<CustomerDto> customers = CustomerApi.getAllCustomers();

            clearCustomerGroups();

            if (customers.isEmpty()) {
                updateStatus("No customers found. Add customers to see the visualization.");
                return;
            }

            visualizeCustomers(customers);
            updateStatus("Displaying " + customers.size() + " customers in 3D");
        } catch (Exception ex) {
            handleError("Failed to load customers", ex);
        }
    }

    private void visualizeCustomers(List<CustomerDto> customers) {
        Random random = new Random();
        int count = customers.size();
        int cols = (int) Math.ceil(Math.sqrt(count));
        int rows = (int) Math.ceil((double) count / cols);

        double spacing = Math.max(120, Math.min(220, 1500d / Math.max(cols, rows)));
        double startX = -(cols - 1) * spacing / 2;
        double startZ = -(rows - 1) * spacing / 2;

        createBasePlatform(cols, rows, spacing, startX, startZ);

        for (int i = 0; i < count; i++) {
            CustomerDto customer = customers.get(i);
            int col = i % cols;
            int row = i / cols;
            double x = startX + col * spacing;
            double z = startZ + row * spacing;

            Group bar = createCustomerBar(customer, i, x, z, random);
            customerGroups.add(bar);
            root3D.getChildren().add(bar);
        }
    }

    private Group createCustomerBar(CustomerDto customer, int index, double x, double z, Random random) {
        Group group = new Group();

        String name = customer.getName();
        int nameLength = (name == null || name.isBlank()) ? 5 : name.length();
        double barHeight = 30 + (nameLength * 8);

        Box bar = new Box(40, barHeight, 40);
        int colorSeed = customer.getCustomerId() != null
                ? customer.getCustomerId().intValue()
                : (name != null ? name.hashCode() : index);
        random.setSeed(colorSeed);
        Color baseColor = Color.hsb(
                Math.abs(colorSeed % 360),
                0.55 + random.nextDouble() * 0.3,
                0.65 + random.nextDouble() * 0.3
        );
        PhongMaterial material = new PhongMaterial(baseColor);
        material.setSpecularColor(Color.WHITE);
        bar.setMaterial(material);

        bar.setTranslateX(x);
        bar.setTranslateY(-barHeight / 2);
        bar.setTranslateZ(z);

        RotateTransition sway = new RotateTransition(Duration.seconds(4 + (index * 0.15)), bar);
        sway.setAxis(Rotate.Y_AXIS);
        sway.setFromAngle(-5);
        sway.setToAngle(5);
        sway.setAutoReverse(true);
        sway.setCycleCount(javafx.animation.Animation.INDEFINITE);
        sway.play();

        group.getChildren().add(bar);
        group.getChildren().add(createLabel(customer, barHeight, x, z));

        Box base = new Box(50, 5, 50);
        base.setMaterial(new PhongMaterial(Color.color(0.35, 0.35, 0.38)));
        base.setTranslateX(x);
        base.setTranslateY(2.5);
        base.setTranslateZ(z);
        group.getChildren().add(base);

        return group;
    }

    private Group createLabel(CustomerDto customer, double barHeight, double x, double z) {
        String displayName = customer.getName();
        if (displayName == null || displayName.isBlank()) {
            displayName = "Unknown";
        } else if (displayName.length() > 18) {
            displayName = displayName.substring(0, 15) + "...";
        }

        Text text = new Text(displayName);
        text.setFill(Color.WHITE);
        text.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        double estimatedWidth = Math.max(80, displayName.length() * 7);
        double estimatedHeight = 18;

        double bgWidth = estimatedWidth + 12;
        double bgHeight = estimatedHeight + 6;

        Rectangle bg = new Rectangle(bgWidth, bgHeight);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(Color.color(0, 0, 0, 0.65));
        bg.setStroke(Color.color(1, 1, 1, 0.35));
        bg.setTranslateX(-bgWidth / 2);
        bg.setTranslateY(-bgHeight / 2);

        text.setTranslateX(-estimatedWidth / 2);
        text.setTranslateY(estimatedHeight / 4);

        Group labelGroup = new Group(bg, text);
        labelGroup.setTranslateX(x);
        labelGroup.setTranslateY(-barHeight - 35);
        labelGroup.setTranslateZ(z);

        return labelGroup;
    }

    private void createBasePlatform(int cols, int rows, double spacing, double startX, double startZ) {
        if (platform != null) {
            root3D.getChildren().remove(platform);
        }
        double platformWidth = cols * spacing + 120;
        double platformDepth = rows * spacing + 120;
        platform = new Box(platformWidth, 6, platformDepth);
        PhongMaterial material = new PhongMaterial(Color.color(0.2, 0.22, 0.28));
        platform.setMaterial(material);

        platform.setTranslateX(startX + (cols - 1) * spacing / 2);
        platform.setTranslateY(3);
        platform.setTranslateZ(startZ + (rows - 1) * spacing / 2);

        root3D.getChildren().add(platform);
    }

    private void clearCustomerGroups() {
        for (Group group : customerGroups) {
            root3D.getChildren().remove(group);
        }
        customerGroups.clear();
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

            rotateY.setAngle(rotateY.getAngle() + deltaX * 0.5);
            rotateX.setAngle(rotateX.getAngle() - deltaY * 0.5);
        });
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

