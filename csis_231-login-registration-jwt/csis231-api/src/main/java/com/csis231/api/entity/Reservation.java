package com.csis231.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private Table table;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @NotNull
    @Column(nullable = false)
    private LocalDateTime datetime;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "res_status", nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.CONFIRMED;
    
    public enum ReservationStatus {
        CONFIRMED, CANCELLED
    }
}
