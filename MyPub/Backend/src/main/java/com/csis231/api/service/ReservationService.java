package com.csis231.api.service;

import com.csis231.api.dto.ReservationDto;
import com.csis231.api.entity.*;
import com.csis231.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;
    private final CustomerRepository customerRepository;

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReservationDto getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));
        return convertToDto(reservation);
    }

    public ReservationDto createReservation(ReservationDto reservationDto) {
        Table table = tableRepository.findById(reservationDto.getTableId())
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + reservationDto.getTableId()));

        Customer customer = customerRepository.findById(reservationDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + reservationDto.getCustomerId()));

        // Prevent reservation if table is already occupied
        if (!isTableAvailableAtTime(table, reservationDto.getDatetime(), null)) {
            throw new RuntimeException("Table is already reserved at this time");
        }

        Reservation reservation = Reservation.builder()
                .table(table)
                .customer(customer)
                .datetime(reservationDto.getDatetime())
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // Update table status if reservation is for now or very soon
        if (reservationDto.getDatetime().isBefore(LocalDateTime.now().plusHours(1))) {
            table.setStatus(Table.TableStatus.RESERVED);
            tableRepository.save(table);
        }

        return convertToDto(savedReservation);
    }

    public ReservationDto updateReservation(Long id, ReservationDto reservationDto) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Cannot update cancelled reservations");
        }

        // Determine new table
        Table newTable = reservation.getTable();
        if (reservationDto.getTableId() != null && !reservationDto.getTableId().equals(reservation.getTable().getTableId())) {
            newTable = tableRepository.findById(reservationDto.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found with id: " + reservationDto.getTableId()));
        }

        // Determine new datetime
        LocalDateTime newDatetime = reservationDto.getDatetime() != null ? reservationDto.getDatetime() : reservation.getDatetime();

        // Check if new table + datetime is available, exclude current reservation
        if (!isTableAvailableAtTime(newTable, newDatetime, reservation.getReservationId())) {
            throw new RuntimeException("Table is not available at the requested time");
        }

        // Update reservation
        reservation.setTable(newTable);
        reservation.setDatetime(newDatetime);

        if (reservationDto.getStatus() != null) {
            reservation.setStatus(reservationDto.getStatus());
        }

        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToDto(savedReservation);
    }

    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

        Table table = reservation.getTable();

        // Only mark table as available if there are no other confirmed reservations at the same datetime
        boolean hasOtherReservations = reservationRepository.findByTable(table).stream()
                .anyMatch(r -> !r.getReservationId().equals(reservation.getReservationId()) &&
                        r.getStatus() == Reservation.ReservationStatus.CONFIRMED &&
                        r.getDatetime().equals(reservation.getDatetime()));

        if (!hasOtherReservations) {
            table.setStatus(Table.TableStatus.AVAILABLE);
            tableRepository.save(table);
        }

        reservationRepository.delete(reservation);
    }

    public List<ReservationDto> getReservationsByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ReservationDto> getReservationsByDateRange(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findByDatetimeBetween(start, end).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    private boolean isTableAvailableAtTime(Table table, LocalDateTime datetime, Long excludeReservationId) {
        return reservationRepository.findByTable(table).stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CONFIRMED)
                .filter(r -> excludeReservationId == null || !r.getReservationId().equals(excludeReservationId))
                .noneMatch(r -> r.getDatetime().equals(datetime));
    }

    private ReservationDto convertToDto(Reservation reservation) {
        return ReservationDto.builder()
                .reservationId(reservation.getReservationId())
                .tableId(reservation.getTable().getTableId())
                .customerId(reservation.getCustomer().getCustomerId())
                .datetime(reservation.getDatetime())
                .status(reservation.getStatus())
                .build();
    }
}
