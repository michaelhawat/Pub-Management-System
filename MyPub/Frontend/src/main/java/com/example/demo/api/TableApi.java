package com.example.demo.api;

import com.example.demo.model.TableDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;
import java.util.List;

public final class TableApi {
    private TableApi() {}
    
    private static final ObjectMapper M = new ObjectMapper();
    
    public static List<TableDto> getAllTables() throws Exception {
        HttpResponse<String> res = ApiClient.get("/api/tables");
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), new TypeReference<List<TableDto>>() {});
    }
    
    public static TableDto getTableById(Long id) throws Exception {
        HttpResponse<String> res = ApiClient.get("/api/tables/" + id);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), TableDto.class);
    }
    
    public static List<TableDto> getTablesByStatus(String status) throws Exception {
        HttpResponse<String> res = ApiClient.get("/api/tables/status/" + status);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), new TypeReference<List<TableDto>>() {});
    }
    
    public static List<TableDto> getAvailableTables(Integer capacity) throws Exception {
        String url = "/api/tables/available";
        if (capacity != null) {
            url += "?capacity=" + capacity;
        }
        HttpResponse<String> res = ApiClient.get(url);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), new TypeReference<List<TableDto>>() {});
    }
    
    public static TableDto createTable(TableDto table) throws Exception {
        String body = M.writeValueAsString(table);
        HttpResponse<String> res = ApiClient.post("/api/tables", body);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), TableDto.class);
    }
    
    public static TableDto updateTable(Long id, TableDto table) throws Exception {
        String body = M.writeValueAsString(table);
        HttpResponse<String> res = ApiClient.put("/api/tables/" + id, body);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), TableDto.class);
    }
    
    public static void deleteTable(Long id) throws Exception {
        HttpResponse<String> res = ApiClient.delete("/api/tables/" + id);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
    }
    
    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "<empty body>" : s;
    }
}
