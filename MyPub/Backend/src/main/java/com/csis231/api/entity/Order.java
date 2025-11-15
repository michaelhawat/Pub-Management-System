package com.csis231.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @JoinColumn( name = "name" , nullable = false)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private Table table;
    
    @NotNull
    @Column(nullable = false)
    private LocalDateTime datetime;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "o_status", nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.OPEN;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    public BigDecimal getTotal() {
        if (orderItems == null || orderItems.isEmpty()) return BigDecimal.ZERO;
        return orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public enum OrderStatus {
        OPEN, CLOSED, PAID
    }
}
