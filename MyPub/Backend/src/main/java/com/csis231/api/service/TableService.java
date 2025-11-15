package com.csis231.api.service;

import com.csis231.api.dto.TableDto;
import com.csis231.api.entity.Table;
import com.csis231.api.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TableService {
    
    private final TableRepository tableRepository;
    
    public List<TableDto> getAllTables() {
        return tableRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public TableDto getTableById(Long id) {
        Table table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + id));
        return convertToDto(table);
    }
    
    public TableDto createTable(TableDto tableDto) {
        log.info("Creating table with capacity: {} and status: {}", tableDto.getCapacity(), tableDto.getStatus());
        Table table = convertToEntity(tableDto);
        Table savedTable = tableRepository.save(table);
        log.info("Table created successfully with ID: {}", savedTable.getTableId());
        return convertToDto(savedTable);
    }
    
    public TableDto updateTable(Long id, TableDto tableDto) {
        Table table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + id));
        
        table.setCapacity(tableDto.getCapacity());
        table.setStatus(tableDto.getStatus());
        
        Table savedTable = tableRepository.save(table);
        return convertToDto(savedTable);
    }
    
    public void deleteTable(Long id) {
        if (!tableRepository.existsById(id)) {
            throw new RuntimeException("Table not found with id: " + id);
        }
        tableRepository.deleteById(id);
    }
    
    public List<TableDto> getTablesByStatus(Table.TableStatus status) {
        return tableRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<TableDto> getAvailableTables(Integer capacity) {
        return tableRepository.findByCapacityGreaterThanEqual(capacity).stream()
                .filter(table -> table.getStatus() == Table.TableStatus.AVAILABLE)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private TableDto convertToDto(Table table) {
        return TableDto.builder()
                .tableId(table.getTableId())
                .capacity(table.getCapacity())
                .status(table.getStatus())
                .build();
    }
    
    private Table convertToEntity(TableDto tableDto) {
        return Table.builder()
                .capacity(tableDto.getCapacity())
                .status(tableDto.getStatus())
                .build();
    }
}
