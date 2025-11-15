package com.csis231.api.dto;

import com.csis231.api.entity.Reservation;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDto {
    private Long reservationId;
    
    @NotNull
    private Long tableId;
    
    @NotNull
    private Long customerId;
    
    @NotNull
    private LocalDateTime datetime;
    
    @NotNull
    private Reservation.ReservationStatus status;
}
