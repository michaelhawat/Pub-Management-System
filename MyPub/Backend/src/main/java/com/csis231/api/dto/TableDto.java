package com.csis231.api.dto;

import com.csis231.api.entity.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableDto {
    private Long tableId;
    
    @NotNull
    @Min(value = 1, message = "Capacity must be greater than 0")
    private Integer capacity;
    
    @NotNull
    private Table.TableStatus status;
}
