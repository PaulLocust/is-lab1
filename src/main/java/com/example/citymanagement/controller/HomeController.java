package com.example.citymanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "Home Controller", description = "Контроллер для перенаправления на главную страницу")
public class HomeController {

    @Operation(summary = "Перенаправление на страницу городов", description = "Корневой URL перенаправляет на список городов")
    @GetMapping("/")
    public String home() {
        return "redirect:/cities";
    }
}