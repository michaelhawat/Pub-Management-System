package com.example.demo.api;

import com.example.demo.model.ReservationDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

public final class ReservationApi {
    private ReservationApi() {}

    private static final ObjectMapper M = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static List<ReservationDto> getAllReservations() throws Exception {
        HttpResponse<String> res = ApiClient.get("/api/reservations");
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), new TypeReference<List<ReservationDto>>() {});
    }

    public static ReservationDto getReservationById(Long id) throws Exception {
        HttpResponse<String> res = ApiClient.get("/api/reservations/" + id);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), ReservationDto.class);
    }

    public static List<ReservationDto> getReservationsByStatus(String status) throws Exception {
        HttpResponse<String> res = ApiClient.get("/api/reservations/status/" + status);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), new TypeReference<List<ReservationDto>>() {});
    }

    public static List<ReservationDto> getReservationsByDateRange(LocalDateTime start, LocalDateTime end) throws Exception {
        String url = "/api/reservations/date-range?start=" + start + "&end=" + end;
        HttpResponse<String> res = ApiClient.get(url);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), new TypeReference<List<ReservationDto>>() {});
    }

    public static ReservationDto createReservation(ReservationDto reservation) throws Exception {
        String body = M.writeValueAsString(reservation);
        HttpResponse<String> res = ApiClient.post("/api/reservations", body);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), ReservationDto.class);
    }

    public static ReservationDto updateReservation(Long id, ReservationDto reservation) throws Exception {
        String body = M.writeValueAsString(reservation);
        HttpResponse<String> res = ApiClient.put("/api/reservations/" + id, body);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return M.readValue(res.body(), ReservationDto.class);
    }

    public static void deleteReservation(Long id) throws Exception {
        HttpResponse<String> res = ApiClient.delete("/api/reservations/" + id);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "<empty body>" : s;
    }
}
