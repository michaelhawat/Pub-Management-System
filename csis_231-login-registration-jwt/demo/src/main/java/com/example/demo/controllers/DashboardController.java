package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.OrderApi;
import com.example.demo.api.TableApi;
import com.example.demo.model.OrderDto;
import com.example.demo.model.TableDto;
import com.example.demo.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;

public class DashboardController {
    
    @FXML private TableView<OrderDto> openOrdersTable;
    @FXML private TableColumn<OrderDto, Long> orderIdColumn;
    @FXML private TableColumn<OrderDto, Long> customerIdColumn;
    @FXML private TableColumn<OrderDto, String> customerNameColumn;
    @FXML private TableColumn<OrderDto, Long> tableIdColumn;
    @FXML private TableColumn<OrderDto, String> statusColumn;

    @FXML private TableView<TableDto> tablesTable;
    @FXML private TableColumn<TableDto, Long> tableIdColumn2;
    @FXML private TableColumn<TableDto, Integer> capacityColumn;
    @FXML private TableColumn<TableDto, String> tableStatusColumn;
    
    @FXML private Label totalOrdersLabel;
    @FXML private Label openOrdersLabel;
    @FXML private Label occupiedTablesLabel;
    
    private ObservableList<OrderDto> openOrders = FXCollections.observableArrayList();
    private ObservableList<TableDto> tables = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTables();
        loadData();
    }
    
    private void setupTables() {
        // Open Orders Table
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableIdColumn.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        openOrdersTable.setItems(openOrders);
        
        // Tables Table
        tableIdColumn2.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        tableStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        tablesTable.setItems(tables);
    }

    private void loadData() {
        try {
            // Load open orders
            List<OrderDto> allOrders = OrderApi.getAllOrders();
            openOrders.clear();

            for (OrderDto order : allOrders) {
                if ("OPEN".equals(order.getStatus())) {
                    order.setTotal(calculateOrderTotal(order)); // calculate total as BigDecimal
                    openOrders.add(order);
                }
            }

            // Load tables
            List<TableDto> allTables = TableApi.getAllTables();
            tables.clear();
            tables.addAll(allTables);

            // Update statistics
            updateStatistics(allOrders, allTables);

        } catch (Exception e) {
            AlertUtils.error("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BigDecimal calculateOrderTotal(OrderDto order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (var item : order.getOrderItems()) {
            if (item.getSubtotal() != null) {
                total = total.add(item.getSubtotal());
            }
        }
        return total.setScale(2, BigDecimal.ROUND_HALF_UP);
    }


    private void updateStatistics(List<OrderDto> orders, List<TableDto> tables) {
        totalOrdersLabel.setText(String.valueOf(orders.size()));
        openOrdersLabel.setText(String.valueOf(orders.stream()
                .filter(order -> "OPEN".equals(order.getStatus()))
                .count()));
        occupiedTablesLabel.setText(String.valueOf(tables.stream()
                .filter(table -> "OCCUPIED".equals(table.getStatus()))
                .count()));

    }
    
    @FXML
    public void refreshData() {
        loadData();
    }
    
    @FXML
    public void goToOrders() {
        Launcher.go("orders.fxml", "Order Management");
    }
    
    @FXML
    public void goToProducts() {
        Launcher.go("products.fxml", "Product Management");
    }
    
    @FXML
    public void goToTables() {
        Launcher.go("tables.fxml", "Table Management");
    }
    
    @FXML
    public void goToReservations() {
        Launcher.go("reservations.fxml", "Reservation Management");
    }
    
    @FXML
    public void goToCustomers() {
        Launcher.go("customers.fxml", "Customer Management");
    }
    
    @FXML
    public void logout() {
        Launcher.go("login.fxml", "Login");
    }
}