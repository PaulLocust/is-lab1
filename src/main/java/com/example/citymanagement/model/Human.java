package com.example.citymanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "humans")
public class Human {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "height", nullable = false)
    @DecimalMin(value = "0.01", message = "Height must be greater than 0")
    private float height;
    
    // Constructors
    public Human() {}
    
    public Human(float height) {
        this.height = height;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
    
    @Override
    public String toString() {
        return "Human{" +
                "height=" + height +
                '}';
    }
}
