package com.example.citymanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "coordinates")
public class Coordinates {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "x", nullable = false)
    private float x;
    
    @Column(name = "y", nullable = false)
    @DecimalMin(value = "-958.99", message = "Y coordinate must be greater than -959")
    private long y;
    
    // Constructors
    public Coordinates() {}
    
    public Coordinates(float x, long y) {
        this.x = x;
        this.y = y;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public long getY() {
        return y;
    }
    
    public void setY(long y) {
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
