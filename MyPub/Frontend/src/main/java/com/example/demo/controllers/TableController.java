package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.TableApi;
import com.example.demo.model.TableDto;
import com.example.demo.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class TableController {
    
    @FXML private TableView<TableDto> tablesTable;
    @FXML private TableColumn<TableDto, Long> tableIdColumn;
    @FXML private TableColumn<TableDto, Integer> capacityColumn;
    @FXML private TableColumn<TableDto, String> statusColumn;
    
    @FXML private TextField capacityField;
    @FXML private ComboBox<String> statusCombo;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    
    private ObservableList<TableDto> tables = FXCollections.observableArrayList();
    private TableDto selectedTable;
    
    @FXML
    public void initialize() {
        setupTable();
        setupComboBox();
        loadTables();
        setupEventHandlers();
    }
    
    private void setupTable() {
        tableIdColumn.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        tablesTable.setItems(tables);
        
        tablesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedTable = newSelection;
                    if (newSelection != null) {
                        populateFields(newSelection);
                        updateButton.setDisable(false);
                        deleteButton.setDisable(false);
                    } else {
                        clearFields();
                        updateButton.setDisable(true);
                        deleteButton.setDisable(true);
                    }
                });
    }
    
    private void setupComboBox() {
        statusCombo.getItems().addAll("AVAILABLE", "OCCUPIED", "RESERVED");
        statusCombo.setValue("AVAILABLE"); // Set default value
    }
    
    private void setupEventHandlers() {
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    private void loadTables() {
        try {
            List<TableDto> tableList = TableApi.getAllTables();
            tables.clear();
            tables.addAll(tableList);
        } catch (Exception e) {
            AlertUtils.error("Error loading tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void populateFields(TableDto table) {
        capacityField.setText(table.getCapacity().toString());
        statusCombo.setValue(table.getStatus());
    }
    
    private void clearFields() {
        capacityField.clear();
        statusCombo.getSelectionModel().clearSelection();
    }
    
    @FXML
    public void addTable() {
        try {
            String capacityText = capacityField.getText().trim();
            String status = statusCombo.getValue();
            
            if (capacityText.isEmpty() || status == null) {
                AlertUtils.warn("Please fill in all fields");
                return;
            }
            
            Integer capacity = Integer.parseInt(capacityText);
            if (capacity <= 0) {
                AlertUtils.warn("Capacity must be greater than 0");
                return;
            }
            
            TableDto newTable = new TableDto(null, capacity, status);
            TableDto createdTable = TableApi.createTable(newTable);
            
            tables.add(createdTable);
            clearFields();
            AlertUtils.info("Table added successfully");
            
        } catch (NumberFormatException e) {
            AlertUtils.warn("Please enter a valid capacity");
        } catch (Exception e) {
            AlertUtils.error("Error adding table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void updateTable() {
        if (selectedTable == null) {
            AlertUtils.warn("Please select a table to update");
            return;
        }
        
        try {
            String capacityText = capacityField.getText().trim();
            String status = statusCombo.getValue();
            
            if (capacityText.isEmpty() || status == null) {
                AlertUtils.warn("Please fill in all fields");
                return;
            }
            
            Integer capacity = Integer.parseInt(capacityText);
            if (capacity <= 0) {
                AlertUtils.warn("Capacity must be greater than 0");
                return;
            }
            
            selectedTable.setCapacity(capacity);
            selectedTable.setStatus(status);
            
            TableApi.updateTable(selectedTable.getTableId(), selectedTable);
            
            tablesTable.refresh();
            clearFields();
            AlertUtils.info("Table updated successfully");
            
        } catch (NumberFormatException e) {
            AlertUtils.warn("Please enter a valid capacity");
        } catch (Exception e) {
            AlertUtils.error("Error updating table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void deleteTable() {
        if (selectedTable == null) {
            AlertUtils.warn("Please select a table to delete");
            return;
        }
        
        if (AlertUtils.confirm("Are you sure you want to delete this table?")) {
            try {
                TableApi.deleteTable(selectedTable.getTableId());
                tables.remove(selectedTable);
                clearFields();
                AlertUtils.info("Table deleted successfully");
            } catch (Exception e) {
                AlertUtils.error("Error deleting table: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    public void refreshTables() {
        loadTables();
    }
    
    @FXML
    public void openVisualization() {
        Launcher.go("table-visualization.fxml", "3D Table Reservation System");
    }
    
    @FXML
    public void goToDashboard() {
        Launcher.go("dashboard.fxml", "Dashboard");
    }
}
