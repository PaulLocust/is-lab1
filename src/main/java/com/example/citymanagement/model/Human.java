package com.example.citymanagement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "humans")
@Schema(description = "Модель человека (губернатора)")
public class Human {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор человека", example = "1")
    private Long id;

    @Column(name = "height", nullable = false)
    @DecimalMin(value = "0.01", message = "Height must be greater than 0")
    @Schema(description = "Рост человека в метрах", example = "1.85", requiredMode = Schema.RequiredMode.REQUIRED)
    private float height;

    // Constructors
    public Human() {}

    public Human(float height) {
        this.height = height;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    @Override
    public String toString() {
        return "Human{" +
                "height=" + height +
                '}';
    }
}