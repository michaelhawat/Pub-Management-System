package com.example.demo.controllers;

import
        com.example.demo.Launcher;
import com.example.demo.api.*;
import com.example.demo.model.*;
import com.example.demo.util.AlertUtils;
import com.example.demo.util.InvoiceGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderController {
    
    @FXML private TableView<OrderDto> ordersTable;
    @FXML private TableColumn<OrderDto, Long> orderIdColumn;
    @FXML private TableColumn<OrderDto, Long> customerIdColumn;
    @FXML private TableColumn<OrderDto, String> customerNameColumn;
    @FXML private TableColumn<OrderDto, Long> tableIdColumn;
    @FXML private TableColumn<OrderDto, String> statusColumn;
    @FXML private TableColumn<OrderDto, LocalDateTime> datetimeColumn;
    @FXML private TableColumn<OrderDto, String> totalColumn;


    @FXML private ComboBox<CustomerDto> customerCombo;
    @FXML private ComboBox<TableDto> tableCombo;
    @FXML private Button createOrderButton;
    @FXML private Button closeOrderButton;
    @FXML private Button deleteOrderButton;
    
    @FXML private TableView<OrderItemDto> orderItemsTable;
    @FXML private TableColumn<OrderItemDto, Long> productIdColumn;
    @FXML private TableColumn<OrderItemDto, String> productNameColumn;
    @FXML private TableColumn<OrderItemDto, Integer> quantityColumn;
    @FXML private TableColumn<OrderItemDto, String> subtotalColumn;


    @FXML private ComboBox<ProductDto> productCombo;
    @FXML private TextField quantityField;
    @FXML private Button addItemButton;
    @FXML private Button removeItemButton;
    
    private ObservableList<OrderDto> orders = FXCollections.observableArrayList();
    private ObservableList<CustomerDto> customers = FXCollections.observableArrayList();
    private ObservableList<TableDto> tables = FXCollections.observableArrayList();
    private ObservableList<ProductDto> products = FXCollections.observableArrayList();
    private ObservableList<OrderItemDto> orderItems = FXCollections.observableArrayList();
    
    private OrderDto selectedOrder;
    private OrderItemDto selectedOrderItem;
    
    @FXML
    public void initialize() {
        setupTables();
        setupComboBoxes();
        loadData();
        setupEventHandlers();
    }
    
    private void setupTables() {
        // Orders Table
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableIdColumn.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        datetimeColumn.setCellValueFactory(new PropertyValueFactory<>("datetime"));
        ordersTable.setItems(orders);
        totalColumn.setCellValueFactory(cellData -> {
            BigDecimal total = cellData.getValue().getTotal();
            return new javafx.beans.property.SimpleStringProperty("$" + total.toString());

        });

        
        // Order Items Table
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        orderItemsTable.setItems(orderItems);



        // Selection listeners
        ordersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedOrder = newSelection;
                    if (newSelection != null) {
                        loadOrderItems(newSelection.getOrderId());
                        closeOrderButton.setDisable(!"OPEN".equals(newSelection.getStatus()));
                        deleteOrderButton.setDisable(!"OPEN".equals(newSelection.getStatus()));
                    } else {
                        orderItems.clear();
                        closeOrderButton.setDisable(true);
                        deleteOrderButton.setDisable(true);
                    }
                });
        
        orderItemsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedOrderItem = newSelection;
                    removeItemButton.setDisable(newSelection == null);
                });
    }
    
    private void setupComboBoxes() {
        customerCombo.setItems(customers);
        customerCombo.setCellFactory(param -> new ListCell<CustomerDto>() {
            @Override
            protected void updateItem(CustomerDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getContact() + ")");
                }
            }
        });
        customerCombo.setButtonCell(new ListCell<CustomerDto>() {
            @Override
            protected void updateItem(CustomerDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getContact() + ")");
                }
            }
        });
        
        tableCombo.setItems(tables);
        tableCombo.setCellFactory(param -> new ListCell<TableDto>() {
            @Override
            protected void updateItem(TableDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Table " + item.getTableId() + " (Capacity: " + item.getCapacity() + ")");
                }
            }
        });
        tableCombo.setButtonCell(new ListCell<TableDto>() {
            @Override
            protected void updateItem(TableDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Table " + item.getTableId() + " (Capacity: " + item.getCapacity() + ")");
                }
            }
        });
        
        productCombo.setItems(products);
        productCombo.setCellFactory(param -> new ListCell<ProductDto>() {
            @Override
            protected void updateItem(ProductDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - $" + item.getPrice());
                }
            }
        });
        productCombo.setButtonCell(new ListCell<ProductDto>() {
            @Override
            protected void updateItem(ProductDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - $" + item.getPrice());
                }
            }
        });
    }
    
    private void setupEventHandlers() {
        closeOrderButton.setDisable(true);
        deleteOrderButton.setDisable(true);
        removeItemButton.setDisable(true);
    }
    
    private void loadData() {
        try {
            // Load orders
            List<OrderDto> orderList = OrderApi.getAllOrders();
            orders.clear();
            orders.addAll(orderList);
            
            // Load customers
            List<CustomerDto> customerList = CustomerApi.getAllCustomers();
            customers.clear();
            customers.addAll(customerList);
            
            // Load available tables
            List<TableDto> tableList = TableApi.getAvailableTables(null);
            tables.clear();
            tables.addAll(tableList);
            
            // Load available products
            List<ProductDto> productList = ProductApi.getAvailableProducts();
            products.clear();
            products.addAll(productList);
            
        } catch (Exception e) {
            AlertUtils.error("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadOrderItems(Long orderId) {
        try {
            List<OrderItemDto> items = OrderItemApi.getOrderItems(orderId);
            orderItems.clear();
            orderItems.addAll(items);
        } catch (Exception e) {
            AlertUtils.error("Error loading order items: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void createOrder() {
        try {
            CustomerDto customer = customerCombo.getSelectionModel().getSelectedItem();
            TableDto table = tableCombo.getSelectionModel().getSelectedItem();
            
            if (customer == null || table == null) {
                AlertUtils.warn("Please select both customer and table");
                return;
            }
            
            OrderDto newOrder = new OrderDto(null, customer.getCustomerId(),customer.getName(),
                    table.getTableId(), LocalDateTime.now(), "OPEN", null , BigDecimal.ZERO);
            
            OrderDto createdOrder = OrderApi.createOrder(newOrder);
            orders.add(createdOrder);
            
            customerCombo.getSelectionModel().clearSelection();
            tableCombo.getSelectionModel().clearSelection();
            
            AlertUtils.info("Order created successfully");
            
        } catch (Exception e) {
            AlertUtils.error("Error creating order: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void closeOrder() {
        if (selectedOrder == null) {
            AlertUtils.warn("Please select an order to close");
            return;
        }
        
        try {
            OrderApi.closeOrder(selectedOrder.getOrderId());
            selectedOrder.setStatus("CLOSED");
            ordersTable.refresh();
            loadOrderItems(selectedOrder.getOrderId());
            AlertUtils.info("Order closed successfully");
            
        } catch (Exception e) {
            AlertUtils.error("Error closing order: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void deleteOrder() {
        if (selectedOrder == null) {
            AlertUtils.warn("Please select an order to delete");
            return;
        }
        
        if (AlertUtils.confirm("Are you sure you want to delete this order?")) {
            try {
                OrderApi.deleteOrder(selectedOrder.getOrderId());
                orders.remove(selectedOrder);
                orderItems.clear();
                AlertUtils.info("Order deleted successfully");
            } catch (Exception e) {
                AlertUtils.error("Error deleting order: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    public void addOrderItem() {
        if (selectedOrder == null) {
            AlertUtils.warn("Please select an order first");
            return;
        }
        
        try {
            ProductDto product = productCombo.getSelectionModel().getSelectedItem();
            String quantityText = quantityField.getText().trim();
            
            if (product == null || quantityText.isEmpty()) {
                AlertUtils.warn("Please select a product and enter quantity");
                return;
            }
            
            Integer quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                AlertUtils.warn("Quantity must be greater than 0");
                return;
            }
            
            OrderItemDto newItem = new OrderItemDto(null, selectedOrder.getOrderId(), 
                    product.getProductId(),product.getName(), quantity, null);
            
            OrderItemDto createdItem = OrderItemApi.addOrderItem(selectedOrder.getOrderId(), newItem);
            orderItems.add(createdItem);
            
            productCombo.getSelectionModel().clearSelection();
            quantityField.clear();
            
            AlertUtils.info("Item added to order");
            
        } catch (NumberFormatException e) {
            AlertUtils.warn("Please enter a valid quantity");
        } catch (Exception e) {
            AlertUtils.error("Error adding item to order: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void removeOrderItem() {
        if (selectedOrderItem == null) {
            AlertUtils.warn("Please select an item to remove");
            return;
        }
        
        try {
            OrderItemApi.removeOrderItem(selectedOrder.getOrderId(), selectedOrderItem.getId());
            orderItems.remove(selectedOrderItem);
            AlertUtils.info("Item removed from order");
            
        } catch (Exception e) {
            AlertUtils.error("Error removing item from order: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void refreshOrders() {
        loadData();
    }
    
    @FXML
    public void goToDashboard() {
        Launcher.go("dashboard.fxml", "Dashboard");
    }

    @FXML
    public void printInvoice() {
        if (selectedOrder == null) {
            AlertUtils.warn("Please select an order to print.");
            return;
        }
        try {
            List<OrderItemDto> items = OrderItemApi.getOrderItems(selectedOrder.getOrderId());
            InvoiceGenerator.generate(selectedOrder, items);
        } catch (Exception e) {
            AlertUtils.error("Error printing invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
