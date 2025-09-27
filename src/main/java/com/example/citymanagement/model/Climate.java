package com.example.citymanagement.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Климатические зоны")
public enum Climate {
    @Schema(description = "Тропический лес") RAIN_FOREST,
    @Schema(description = "Тропическая саванна") TROPICAL_SAVANNA,
    @Schema(description = "Океанический климат") OCEANIC
}