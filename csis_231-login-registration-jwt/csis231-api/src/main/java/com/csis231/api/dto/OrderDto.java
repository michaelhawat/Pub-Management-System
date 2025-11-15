package com.csis231.api.dto;

import com.csis231.api.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long orderId;
    
    @NotNull
    private Long customerId;

    @NotNull
    private String name;

    @NotNull
    private Long tableId;
    
    @NotNull
    private LocalDateTime datetime;
    
    @NotNull
    private Order.OrderStatus status;
    
    private List<OrderItemDto> orderItems;

    private BigDecimal total;

}
