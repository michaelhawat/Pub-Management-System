package com.csis231.api.controller;

import com.csis231.api.dto.TableDto;
import com.csis231.api.entity.Table;
import com.csis231.api.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@Slf4j
public class TableController {
    
    private final TableService tableService;
    
    @GetMapping
    public ResponseEntity<List<TableDto>> getAllTables() {
        try {
            List<TableDto> tables = tableService.getAllTables();
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("Error fetching tables", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getTableById(@PathVariable Long id) {
        try {
            TableDto table = tableService.getTableById(id);
            return ResponseEntity.ok(table);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "code", "TABLE_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error fetching table with id: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error", "code", "INTERNAL_ERROR"));
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TableDto>> getTablesByStatus(@PathVariable Table.TableStatus status) {
        try {
            List<TableDto> tables = tableService.getTablesByStatus(status);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("Error fetching tables by status: " + status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<TableDto>> getAvailableTables(@RequestParam(required = false) Integer capacity) {
        try {
            List<TableDto> tables = tableService.getAvailableTables(capacity != null ? capacity : 1);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("Error fetching available tables", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createTable(@Valid @RequestBody TableDto tableDto) {
        try {
            log.info("Received request to create table: {}", tableDto);
            TableDto createdTable = tableService.createTable(tableDto);
            log.info("Table created successfully: {}", createdTable);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTable);
        } catch (Exception e) {
            log.error("Error creating table", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid table data: " + e.getMessage(), "code", "INVALID_DATA"));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTable(@PathVariable Long id, @Valid @RequestBody TableDto tableDto) {
        try {
            TableDto updatedTable = tableService.updateTable(id, tableDto);
            return ResponseEntity.ok(updatedTable);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "code", "TABLE_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error updating table with id: " + id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid table data", "code", "INVALID_DATA"));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTable(@PathVariable Long id) {
        try {
            tableService.deleteTable(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "code", "TABLE_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error deleting table with id: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error", "code", "INTERNAL_ERROR"));
        }
    }
}
