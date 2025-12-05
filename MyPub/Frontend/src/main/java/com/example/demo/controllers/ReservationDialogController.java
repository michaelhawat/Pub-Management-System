package com.example.demo.controllers;

import com.example.demo.api.CustomerApi;
import com.example.demo.api.ReservationApi;
import com.example.demo.model.CustomerDto;
import com.example.demo.model.ReservationDto;
import com.example.demo.model.TableDto;
import com.example.demo.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ReservationDialogController {

    @FXML private Label tableInfoLabel;
    @FXML private ComboBox<CustomerDto> customerCombo;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private Button createButton;
    @FXML private Button cancelButton;

    private TableDto table;
    private TableVisualizationController parentController;

    public void setTable(TableDto table) {
        this.table = table;
        if (tableInfoLabel != null) {
            tableInfoLabel.setText("Table #" + table.getTableId() + " - Capacity: " + table.getCapacity() + " seats");
        }
    }

    public void setParentController(TableVisualizationController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        setupSpinners();
        loadCustomers();
        setupDatePicker();
    }

    private void setupSpinners() {
        SpinnerValueFactory<Integer> hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18);
        SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15);
        hourSpinner.setValueFactory(hourFactory);
        minuteSpinner.setValueFactory(minuteFactory);
    }

    private void setupDatePicker() {
        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    private void loadCustomers() {
        try {
            List<CustomerDto> customers = CustomerApi.getAllCustomers();
            customerCombo.getItems().clear();
            customerCombo.getItems().addAll(customers);
            
            customerCombo.setCellFactory(listView -> new ListCell<CustomerDto>() {
                @Override
                protected void updateItem(CustomerDto customer, boolean empty) {
                    super.updateItem(customer, empty);
                    if (empty || customer == null) {
                        setText(null);
                    } else {
                        setText(customer.getName() + " (" + customer.getContact() + ")");
                    }
                }
            });
            
            customerCombo.setButtonCell(new ListCell<CustomerDto>() {
                @Override
                protected void updateItem(CustomerDto customer, boolean empty) {
                    super.updateItem(customer, empty);
                    if (empty || customer == null) {
                        setText("Select Customer");
                    } else {
                        setText(customer.getName() + " (" + customer.getContact() + ")");
                    }
                }
            });
        } catch (Exception ex) {
            AlertUtils.error("Failed to load customers: " + ex.getMessage());
        }
    }

    @FXML
    private void createReservation() {
        if (table == null) {
            AlertUtils.warn("Table information is missing");
            return;
        }

        CustomerDto selectedCustomer = customerCombo.getValue();
        if (selectedCustomer == null) {
            AlertUtils.warn("Please select a customer");
            return;
        }

        LocalDate date = datePicker.getValue();
        if (date == null) {
            AlertUtils.warn("Please select a date");
            return;
        }

        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));

        if (dateTime.isBefore(LocalDateTime.now())) {
            AlertUtils.warn("Reservation time must be in the future");
            return;
        }

        try {
            ReservationDto reservation = new ReservationDto();
            reservation.setTableId(table.getTableId());
            reservation.setCustomerId(selectedCustomer.getCustomerId());
            reservation.setDatetime(dateTime);
            reservation.setStatus("CONFIRMED");

            ReservationApi.createReservation(reservation);
            AlertUtils.info("Reservation created successfully!");

            if (parentController != null) {
                parentController.onReservationCreated();
            }

            closeDialog();
        } catch (Exception ex) {
            AlertUtils.error("Failed to create reservation: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}

