package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TableDto {
    @JsonProperty("tableId")
    private Long tableId;
    
    @JsonProperty("capacity")
    private Integer capacity;
    
    @JsonProperty("status")
    private String status;
    
    public TableDto() {}
    
    public TableDto(Long tableId, Integer capacity, String status) {
        this.tableId = tableId;
        this.capacity = capacity;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
