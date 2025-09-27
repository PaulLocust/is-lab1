package com.example.citymanagement.controller;

import com.example.citymanagement.model.City;
import com.example.citymanagement.model.Climate;
import com.example.citymanagement.model.Coordinates;
import com.example.citymanagement.service.CityService;
import com.example.citymanagement.service.CoordinatesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cities")
@Tag(name = "City Controller", description = "API для управления городами")
public class CityController {

    @Autowired
    private CityService cityService;

    @Autowired
    private CoordinatesService coordinatesService;

    @Operation(summary = "Получить список городов с пагинацией и поиском")
    @GetMapping({"", "/", "/list"})
    public String getAllCities(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Поисковый запрос") @RequestParam(required = false) String search,
            Model model) {

        System.out.println("CityController.getAllCities called with page=" + page + ", size=" + size);

        Page<City> citiesPage;

        if (search != null && !search.trim().isEmpty()) {
            // Используем поиск с пагинацией и сортировкой
            citiesPage = cityService.searchCities(search, page, size, sortBy, sortDir);
            model.addAttribute("search", search);
        } else {
            // Обычный список с пагинацией
            citiesPage = cityService.getCitiesPage(page, size, sortBy, sortDir);
        }

        model.addAttribute("cities", citiesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", citiesPage.getTotalPages());
        model.addAttribute("totalElements", citiesPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("size", size);

        System.out.println("Returning view: cities/list");
        return "cities/list";
    }

    @Operation(summary = "Получить город по ID")
    @GetMapping("/{id}")
    public String getCityById(
            @Parameter(description = "ID города") @PathVariable Long id,
            Model model) {
        City city = cityService.getCityById(id).orElse(null);
        if (city == null) {
            return "error/404";
        }
        model.addAttribute("city", city);
        return "cities/detail";
    }

    @Operation(summary = "Показать форму создания города")
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("city", new City());
        model.addAttribute("coordinates", coordinatesService.getAllCoordinates());
        model.addAttribute("climates", Climate.values());
        return "cities/form";
    }

    @Operation(summary = "Создать новый город")
    @PostMapping
    public String createCity(
            @Parameter(description = "Данные города") @Valid @ModelAttribute City city,
            BindingResult result,
            Model model) {

        processCityBeforeValidation(city);

        if (result.hasErrors()) {
            model.addAttribute("coordinates", coordinatesService.getAllCoordinates());
            model.addAttribute("climates", Climate.values());
            return "cities/form";
        }

        cityService.saveCity(city);
        return "redirect:/cities";
    }

    @Operation(summary = "Показать форму редактирования города")
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @Parameter(description = "ID города") @PathVariable Long id,
            Model model) {
        City city = cityService.getCityById(id).orElse(null);
        if (city == null) {
            return "error/404";
        }
        model.addAttribute("city", city);
        model.addAttribute("coordinates", coordinatesService.getAllCoordinates());
        model.addAttribute("climates", Climate.values());
        return "cities/form";
    }

    @Operation(summary = "Обновить город")
    @PostMapping("/{id}")
    public String updateCity(
            @Parameter(description = "ID города") @PathVariable Long id,
            @Parameter(description = "Данные города") @Valid @ModelAttribute City city,
            BindingResult result,
            Model model) {

        processCityBeforeValidation(city);

        if (result.hasErrors()) {
            model.addAttribute("coordinates", coordinatesService.getAllCoordinates());
            model.addAttribute("climates", Climate.values());
            return "cities/form";
        }

        city.setId(id);
        cityService.saveCity(city);
        return "redirect:/cities";
    }

    @Operation(summary = "Удалить город")
    @GetMapping("/{id}/delete")
    public String deleteCity(
            @Parameter(description = "ID города") @PathVariable Long id,
            Model model) {
        try {
            cityService.deleteCity(id);
            return "redirect:/cities";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error/404";
        }
    }

    // Special operations API endpoints

    @Operation(summary = "Удалить города по типу климата")
    @PostMapping("/special/delete-by-climate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCitiesByClimate(
            @Parameter(description = "Тип климата") @RequestParam Climate climate) {
        Map<String, Object> response = new HashMap<>();
        try {
            long deletedCount = cityService.deleteCitiesByClimate(climate);
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("message", "Deleted " + deletedCount + " cities with climate " + climate);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting cities: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить среднюю высоту над уровнем моря")
    @GetMapping("/special/average-meters")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAverageMetersAboveSeaLevel() {
        Map<String, Object> response = new HashMap<>();
        try {
            Double average = cityService.getAverageMetersAboveSeaLevel();
            response.put("success", true);
            response.put("average", average);
            response.put("message", "Average meters above sea level: " + (average != null ? String.format("%.2f", average) : "N/A"));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error calculating average: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить уникальные коды автомобилей")
    @GetMapping("/special/unique-car-codes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUniqueCarCodes() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> carCodes = cityService.getUniqueCarCodes();
            response.put("success", true);
            response.put("carCodes", carCodes);
            response.put("message", "Found " + carCodes.size() + " unique car codes");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting unique car codes: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Рассчитать расстояние до города с максимальной площадью")
    @GetMapping("/special/distance-to-max-area")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calculateDistanceToCityWithMaxArea() {
        Map<String, Object> response = new HashMap<>();
        try {
            double distance = cityService.calculateDistanceToCityWithMaxArea();
            response.put("success", true);
            response.put("distance", distance);
            response.put("message", "Distance to city with max area: " + String.format("%.2f", distance));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error calculating distance: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Рассчитать расстояние от начала координат до города с максимальным населением")
    @GetMapping("/special/distance-from-origin-to-max-population")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calculateDistanceFromOriginToCityWithMaxPopulation() {
        Map<String, Object> response = new HashMap<>();
        try {
            double distance = cityService.calculateDistanceFromOriginToCityWithMaxPopulation();
            response.put("success", true);
            response.put("distance", distance);
            response.put("message", "Distance from origin to city with max population: " + String.format("%.2f", distance));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error calculating distance: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // Метод для предварительной обработки города
    private void processCityBeforeValidation(City city) {
        // Убедимся, что coordinates не null (обязательное поле)
        if (city.getCoordinates() == null) {
            city.setCoordinates(new Coordinates(0.0f, 0L));
        }

        // Установка даты создания, если не установлена
        if (city.getCreationDate() == null) {
            city.setCreationDate(LocalDateTime.now());
        }

        // Обработка необязательных полей - если пустые строки, то null
        if (city.getMetersAboveSeaLevel() != null && city.getMetersAboveSeaLevel() == 0) {
            city.setMetersAboveSeaLevel(null);
        }

        if (city.getCarCode() != null && city.getCarCode() == 0) {
            city.setCarCode(null);
        }

        // Обработка governor - если высота 0, то устанавливаем governor в null
        if (city.getGovernor() != null && city.getGovernor().getHeight() == 0) {
            city.setGovernor(null);
        }

        // Установка capital по умолчанию, если не выбрано
        if (city.getCapital() == null) {
            city.setCapital(false);
        }
    }
}