package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {
    @JsonProperty("orderId")
    private Long orderId;
    
    @JsonProperty("customerId")
    private Long customerId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("tableId")
    private Long tableId;
    
    @JsonProperty("datetime")
    private LocalDateTime datetime;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("orderItems")
    private List<OrderItemDto> orderItems;

    @JsonProperty("total")
    private BigDecimal total;
    
    public OrderDto() {}
    
    public OrderDto(Long orderId, Long customerId, String name,Long tableId,
                   LocalDateTime datetime, String status, List<OrderItemDto> orderItems , BigDecimal total) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.name = name;
        this.tableId = tableId;
        this.datetime = datetime;
        this.status = status;
        this.orderItems = orderItems;
        this.total = getTotal();
    }
    
    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    
    public LocalDateTime getDatetime() { return datetime; }
    public void setDatetime(LocalDateTime datetime) { this.datetime = datetime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<OrderItemDto> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDto> orderItems) { this.orderItems = orderItems; }

    public BigDecimal getTotal() {
        if (orderItems == null || orderItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        // Assuming OrderItemDto.getSubtotal() returns BigDecimal
        return orderItems.stream()
                .map(OrderItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
