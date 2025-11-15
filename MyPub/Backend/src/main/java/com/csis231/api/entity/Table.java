package com.csis231.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Entity
@jakarta.persistence.Table(name = "tables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Table {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long tableId;
    
    @NotNull
    @Min(value = 1, message = "Capacity must be greater than 0")
    @Column(nullable = false)
    private Integer capacity;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "t_status", nullable = false)
    @Builder.Default
    private TableStatus status = TableStatus.AVAILABLE;
    
    public enum TableStatus {
        AVAILABLE, OCCUPIED, RESERVED
    }
}
