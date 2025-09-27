package com.example.citymanagement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "coordinates")
@Schema(description = "Модель координат")
public class Coordinates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор координат", example = "1")
    private Long id;

    @Column(name = "x", nullable = false)
    @Schema(description = "Координата X", example = "55.7558", requiredMode = Schema.RequiredMode.REQUIRED)
    private float x;

    @Column(name = "y", nullable = false)
    @DecimalMin(value = "-958.99", message = "Y coordinate must be greater than -959")
    @Schema(description = "Координата Y", example = "37.6173", requiredMode = Schema.RequiredMode.REQUIRED)
    private long y;

    // Constructors
    public Coordinates() {}

    public Coordinates(float x, long y) {
        this.x = x;
        this.y = y;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public long getY() { return y; }
    public void setY(long y) { this.y = y; }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}