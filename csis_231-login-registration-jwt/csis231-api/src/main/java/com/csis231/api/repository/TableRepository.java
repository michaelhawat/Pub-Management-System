package com.csis231.api.repository;

import com.csis231.api.entity.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<Table, Long> {
    List<Table> findByStatus(Table.TableStatus status);
    List<Table> findByCapacityGreaterThanEqual(Integer capacity);
}
