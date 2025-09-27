package com.example.citymanagement.controller;

import com.example.citymanagement.model.City;
import com.example.citymanagement.model.Climate;
import com.example.citymanagement.model.Coordinates;
import com.example.citymanagement.service.CityService;
import com.example.citymanagement.service.CoordinatesService;
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
public class CityController {
    
    @Autowired
    private CityService cityService;
    
    @Autowired
    private CoordinatesService coordinatesService;

    @GetMapping({"", "/", "/list"})
    public String getAllCities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
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
    
    @GetMapping("/{id}")
    public String getCityById(@PathVariable Long id, Model model) {
        City city = cityService.getCityById(id).orElse(null);
        if (city == null) {
            return "error/404";
        }
        model.addAttribute("city", city);
        return "cities/detail";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("city", new City());
        model.addAttribute("coordinates", coordinatesService.getAllCoordinates());
        model.addAttribute("climates", Climate.values());
        return "cities/form";
    }
    
    @PostMapping
    public String createCity(@Valid @ModelAttribute City city, BindingResult result, Model model) {

        processCityBeforeValidation(city);

        if (result.hasErrors()) {
            model.addAttribute("coordinates", coordinatesService.getAllCoordinates());
            model.addAttribute("climates", Climate.values());
            return "cities/form";
        }
        
        cityService.saveCity(city);
        return "redirect:/cities";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        City city = cityService.getCityById(id).orElse(null);
        if (city == null) {
            return "error/404";
        }
        model.addAttribute("city", city);
        model.addAttribute("coordinates", coordinatesService.getAllCoordinates());
        model.addAttribute("climates", Climate.values());
        return "cities/form";
    }

    @PostMapping("/{id}")
    public String updateCity(@PathVariable Long id, @Valid @ModelAttribute City city, BindingResult result, Model model) {

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

    @GetMapping("/{id}/delete")  // Изменили с @PostMapping на @GetMapping
    public String deleteCity(@PathVariable Long id, Model model) {
        try {
            cityService.deleteCity(id);
            return "redirect:/cities";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error/404";
        }
    }
    
    // Special operations API endpoints
    @PostMapping("/special/delete-by-climate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCitiesByClimate(@RequestParam Climate climate) {
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


