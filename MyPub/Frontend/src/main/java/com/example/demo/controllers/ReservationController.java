package com.example.demo.controllers;

import com.example.demo.Launcher;
import com.example.demo.api.*;
import com.example.demo.model.*;
import com.example.demo.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationController {
    
    @FXML private TableView<ReservationDto> reservationsTable;
    @FXML private TableColumn<ReservationDto, Long> reservationIdColumn;
    @FXML private TableColumn<ReservationDto, Long> tableIdColumn;
    @FXML private TableColumn<ReservationDto, Long> customerIdColumn;
    @FXML private TableColumn<ReservationDto, LocalDateTime> datetimeColumn;
    @FXML private TableColumn<ReservationDto, String> statusColumn;
    
    @FXML private ComboBox<TableDto> tableCombo;
    @FXML private ComboBox<CustomerDto> customerCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    
    private ObservableList<ReservationDto> reservations = FXCollections.observableArrayList();
    private ObservableList<TableDto> tables = FXCollections.observableArrayList();
    private ObservableList<CustomerDto> customers = FXCollections.observableArrayList();
    private ReservationDto selectedReservation;
    
    @FXML
    public void initialize() {
        setupTable();
        setupComboBoxes();
        loadData();
        setupEventHandlers();
    }
    
    private void setupTable() {
        reservationIdColumn.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        tableIdColumn.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        datetimeColumn.setCellValueFactory(new PropertyValueFactory<>("datetime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        reservationsTable.setItems(reservations);
        
        reservationsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedReservation = newSelection;
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
    
    private void setupComboBoxes() {
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
    }
    
    private void setupEventHandlers() {
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    private void loadData() {
        try {
            // Load reservations
            List<ReservationDto> reservationList = ReservationApi.getAllReservations();
            reservations.clear();
            reservations.addAll(reservationList);
            
            // Load tables
            List<TableDto> tableList = TableApi.getAllTables();
            tables.clear();
            tables.addAll(tableList);
            
            // Load customers
            List<CustomerDto> customerList = CustomerApi.getAllCustomers();
            customers.clear();
            customers.addAll(customerList);
            
        } catch (Exception e) {
            AlertUtils.error("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void populateFields(ReservationDto reservation) {
        // Find and select the table
        for (TableDto table : tables) {
            if (table.getTableId().equals(reservation.getTableId())) {
                tableCombo.getSelectionModel().select(table);
                break;
            }
        }
        
        // Find and select the customer
        for (CustomerDto customer : customers) {
            if (customer.getCustomerId().equals(reservation.getCustomerId())) {
                customerCombo.getSelectionModel().select(customer);
                break;
            }
        }
        
        // Set date and time
        LocalDateTime dateTime = reservation.getDatetime();
        datePicker.setValue(dateTime.toLocalDate());
        timeField.setText(dateTime.toLocalTime().toString());
    }
    
    private void clearFields() {
        tableCombo.getSelectionModel().clearSelection();
        customerCombo.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        timeField.clear();
    }
    
    @FXML
    public void addReservation() {
        try {
            TableDto table = tableCombo.getSelectionModel().getSelectedItem();
            CustomerDto customer = customerCombo.getSelectionModel().getSelectedItem();
            
            if (table == null || customer == null || datePicker.getValue() == null || timeField.getText().isEmpty()) {
                AlertUtils.warn("Please fill in all fields");
                return;
            }
            
            // Improved time parsing for strict format and error handling
            String timeText = timeField.getText().trim();

            // Acceptable: H:mm or HH:mm, but force output to "HH:mm"
            java.time.LocalTime parsedTime;
            try {
                // Accept single or double digit hour, always 2 digits minute
                if (!timeText.matches("\\d{1,2}:\\d{2}")) {
                    AlertUtils.warn("Please enter time in HH:mm format (e.g., 14:30)");
                    return;
                }
                // Parse and format to strict "HH:mm"
                String[] parts = timeText.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                    throw new NumberFormatException();
                }
                parsedTime = java.time.LocalTime.of(hour, minute);
            } catch (Exception ex) {
                AlertUtils.warn("Invalid time. Please enter as HH:mm (e.g., 09:15 or 14:30)");
                return;
            }

            LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), parsedTime);

            ReservationDto newReservation = new ReservationDto(null, table.getTableId(),
                    customer.getCustomerId(), dateTime, "CONFIRMED");

            ReservationDto createdReservation = ReservationApi.createReservation(newReservation);
            reservations.add(createdReservation);

            clearFields();
            AlertUtils.info("Reservation created successfully");

        } catch (Exception e) {
            AlertUtils.error("Error creating reservation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void updateReservation() {
        if (selectedReservation == null) {
            AlertUtils.warn("Please select a reservation to update");
            return;
        }
        
        try {
            TableDto table = tableCombo.getSelectionModel().getSelectedItem();
            CustomerDto customer = customerCombo.getSelectionModel().getSelectedItem();
            
            if (table == null || customer == null || datePicker.getValue() == null || timeField.getText().isEmpty()) {
                AlertUtils.warn("Please fill in all fields");
                return;
            }
            
            LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), 
                    java.time.LocalTime.parse(timeField.getText()));
            
            selectedReservation.setTableId(table.getTableId());
            selectedReservation.setCustomerId(customer.getCustomerId());
            selectedReservation.setDatetime(dateTime);
            
            ReservationApi.updateReservation(selectedReservation.getReservationId(), selectedReservation);
            
            reservationsTable.refresh();
            clearFields();
            AlertUtils.info("Reservation updated successfully");
            
        } catch (Exception e) {
            AlertUtils.error("Error updating reservation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void deleteReservation() {
        if (selectedReservation == null) {
            AlertUtils.warn("Please select a reservation to delete");
            return;
        }
        
        if (AlertUtils.confirm("Are you sure you want to delete this reservation?")) {
            try {
                ReservationApi.deleteReservation(selectedReservation.getReservationId());
                reservations.remove(selectedReservation);
                clearFields();
                AlertUtils.info("Reservation deleted successfully");
            } catch (Exception e) {
                AlertUtils.error("Error deleting reservation: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void openAddCustomerDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Customer");
        dialog.setHeaderText("Create a new customer");
        dialog.setContentText("Enter customer name:");

        dialog.showAndWait().ifPresent(name -> {
            if (name.trim().isEmpty()) {
                AlertUtils.warn("Customer name cannot be empty.");
                return;
            }

            TextInputDialog contactDialog = new TextInputDialog();
            contactDialog.setTitle("Add Customer Contact");
            contactDialog.setHeaderText("Enter customer contact info:");
            contactDialog.setContentText("Phone :");

            contactDialog.showAndWait().ifPresent(contact -> {
                if (contact.trim().isEmpty()) {
                    AlertUtils.warn("Contact cannot be empty.");
                    return;
                }

                try {
                    // Create and save new customer
                    CustomerDto newCustomer = new CustomerDto(null, name.trim(), contact.trim());
                    CustomerDto saved = CustomerApi.createCustomer(newCustomer);

                    // Add to combo and select it
                    customers.add(saved);
                    customerCombo.getSelectionModel().select(saved);

                    AlertUtils.info("Customer added successfully.");
                } catch (Exception e) {
                    AlertUtils.error("Error adding customer: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });
    }


    @FXML
    public void refreshReservations() {
        loadData();
    }
    
    @FXML
    public void goToDashboard() {
        Launcher.go("dashboard.fxml", "Dashboard");
    }
}
