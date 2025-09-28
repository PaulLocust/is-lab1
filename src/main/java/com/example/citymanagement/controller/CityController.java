package com.example.citymanagement.controller;

import com.example.citymanagement.model.City;
import com.example.citymanagement.model.Climate;
import com.example.citymanagement.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cities")
@Tag(name = "City Controller", description = "REST API для управления городами")
public class CityController {

    @Autowired
    private CityService cityService;

    @Operation(summary = "Получить список городов с пагинацией и поиском")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCities(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Поисковый запрос") @RequestParam(required = false) String search) {

        try {
            Page<City> citiesPage;

            if (search != null && !search.trim().isEmpty()) {
                citiesPage = cityService.searchCities(search, page, size, sortBy, sortDir);
            } else {
                citiesPage = cityService.getCitiesPage(page, size, sortBy, sortDir);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("cities", citiesPage.getContent());
            response.put("currentPage", citiesPage.getNumber());
            response.put("totalPages", citiesPage.getTotalPages());
            response.put("totalElements", citiesPage.getTotalElements());
            response.put("sortBy", sortBy);
            response.put("sortDir", sortDir);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error loading cities: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить город по ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCityById(
            @Parameter(description = "ID города") @PathVariable Long id) {

        try {
            Optional<City> city = cityService.getCityById(id);

            if (city.isPresent()) {
                return ResponseEntity.ok(city.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "City not found with id: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error loading city: " + e.getMessage()));
        }
    }

    @Operation(summary = "Создать новый город")
    @PostMapping
    public ResponseEntity<?> createCity(
            @Parameter(description = "Данные города") @Valid @RequestBody City city,
            BindingResult result) {

        try {
            if (result.hasErrors()) {
                Map<String, String> errors = result.getFieldErrors().stream()
                        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
                return ResponseEntity.badRequest().body(Map.of("errors", errors));
            }

            // Автоматическая установка даты создания
            if (city.getCreationDate() == null) {
                city.setCreationDate(LocalDateTime.now());
            }

            City savedCity = cityService.saveCity(city);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error creating city: " + e.getMessage()));
        }
    }

    @Operation(summary = "Обновить город")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCity(
            @Parameter(description = "ID города") @PathVariable Long id,
            @Parameter(description = "Данные города") @Valid @RequestBody City city,
            BindingResult result) {

        try {
            if (result.hasErrors()) {
                Map<String, String> errors = result.getFieldErrors().stream()
                        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
                return ResponseEntity.badRequest().body(Map.of("errors", errors));
            }

            if (!cityService.getCityById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "City not found with id: " + id));
            }

            city.setId(id);
            City updatedCity = cityService.saveCity(city);
            return ResponseEntity.ok(updatedCity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error updating city: " + e.getMessage()));
        }
    }

    @Operation(summary = "Удалить город")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCity(
            @Parameter(description = "ID города") @PathVariable Long id) {

        try {
            if (!cityService.getCityById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "City not found with id: " + id));
            }

            cityService.deleteCity(id);
            return ResponseEntity.ok().body(Map.of("message", "City deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting city: " + e.getMessage()));
        }
    }

    // Special operations API endpoints

    @Operation(summary = "Удалить города по типу климата")
    @PostMapping("/special/delete-by-climate")
    public ResponseEntity<Map<String, Object>> deleteCitiesByClimate(
            @Parameter(description = "Тип климата") @RequestParam Climate climate) {

        Map<String, Object> response = new HashMap<>();
        try {
            long deletedCount = cityService.deleteCitiesByClimate(climate);
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("message", "Deleted " + deletedCount + " cities with climate " + climate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting cities: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Получить среднюю высоту над уровнем моря")
    @GetMapping("/special/average-meters")
    public ResponseEntity<Map<String, Object>> getAverageMetersAboveSeaLevel() {

        Map<String, Object> response = new HashMap<>();
        try {
            Double average = cityService.getAverageMetersAboveSeaLevel();
            response.put("success", true);
            response.put("average", average);
            response.put("message", "Average meters above sea level: " + (average != null ? String.format("%.2f", average) : "N/A"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error calculating average: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Получить уникальные коды автомобилей")
    @GetMapping("/special/unique-car-codes")
    public ResponseEntity<Map<String, Object>> getUniqueCarCodes() {

        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> carCodes = cityService.getUniqueCarCodes();
            response.put("success", true);
            response.put("carCodes", carCodes);
            response.put("message", "Found " + carCodes.size() + " unique car codes");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting unique car codes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Рассчитать расстояние до города с максимальной площадью")
    @GetMapping("/special/distance-to-max-area")
    public ResponseEntity<Map<String, Object>> calculateDistanceToCityWithMaxArea() {

        Map<String, Object> response = new HashMap<>();
        try {
            double distance = cityService.calculateDistanceToCityWithMaxArea();
            response.put("success", true);
            response.put("distance", distance);
            response.put("message", "Distance to city with max area: " + String.format("%.2f", distance));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error calculating distance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Рассчитать расстояние от начала координат до города с максимальным населением")
    @GetMapping("/special/distance-from-origin-to-max-population")
    public ResponseEntity<Map<String, Object>> calculateDistanceFromOriginToCityWithMaxPopulation() {

        Map<String, Object> response = new HashMap<>();
        try {
            double distance = cityService.calculateDistanceFromOriginToCityWithMaxPopulation();
            response.put("success", true);
            response.put("distance", distance);
            response.put("message", "Distance from origin to city with max population: " + String.format("%.2f", distance));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error calculating distance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Получить все доступные типы климата")
    @GetMapping("/climates")
    public ResponseEntity<Climate[]> getClimates() {
        return ResponseEntity.ok(Climate.values());
    }
}