package com.example.citymanagement.controller;

import com.example.citymanagement.model.Human;
import com.example.citymanagement.service.HumanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/humans")
@Tag(name = "Human Controller", description = "API для управления губернаторами")
public class HumanController {

    @Autowired
    private HumanService humanService;

    @Operation(summary = "Получить всех губернаторов")
    @GetMapping
    public ResponseEntity<List<Human>> getAllHumans() {
        try {
            List<Human> humans = humanService.getAllHumans();
            return ResponseEntity.ok(humans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Создать нового губернатора")
    @PostMapping
    public ResponseEntity<Human> createHuman(
            @Valid @RequestBody Human human) {
        try {
            Human savedHuman = humanService.saveHuman(human);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedHuman);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}