package com.csis231.api.repository;

import com.csis231.api.entity.Reservation;
import com.csis231.api.entity.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    List<Reservation> findByTable(Table table);
    List<Reservation> findByDatetimeBetween(LocalDateTime start, LocalDateTime end);
}
