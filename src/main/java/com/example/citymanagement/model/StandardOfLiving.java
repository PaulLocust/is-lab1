package com.example.citymanagement.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Уровень жизни в городе")
public enum StandardOfLiving {
    @Schema(description = "Высокий уровень жизни") HIGH,
    @Schema(description = "Низкий уровень жизни") LOW,
    @Schema(description = "Очень низкий уровень жизни") VERY_LOW
}