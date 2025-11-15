package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ReservationDto {
    @JsonProperty("reservationId")
    private Long reservationId;
    
    @JsonProperty("tableId")
    private Long tableId;
    
    @JsonProperty("customerId")
    private Long customerId;
    
    @JsonProperty("datetime")
    private LocalDateTime datetime;
    
    @JsonProperty("status")
    private String status;
    
    public ReservationDto() {}
    
    public ReservationDto(Long reservationId, Long tableId, Long customerId, 
                        LocalDateTime datetime, String status) {
        this.reservationId = reservationId;
        this.tableId = tableId;
        this.customerId = customerId;
        this.datetime = datetime;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public LocalDateTime getDatetime() { return datetime; }
    public void setDatetime(LocalDateTime datetime) { this.datetime = datetime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
