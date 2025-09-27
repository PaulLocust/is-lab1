package com.example.citymanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cities")
public class City {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive(message = "ID must be greater than 0")
    private Long id;
    
    @Column(name = "name", nullable = false)
    @NotBlank(message = "Name cannot be null or empty")
    private String name;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "coordinates_id", nullable = false)
    @NotNull(message = "Coordinates cannot be null")
    private Coordinates coordinates;
    
    @Column(name = "creation_date", nullable = false)
    @NotNull(message = "Creation date cannot be null")
    private LocalDateTime creationDate;
    
    @Column(name = "area", nullable = false)
    @NotNull(message = "Area cannot be null")
    @Positive(message = "Area must be greater than 0")
    private Integer area;
    
    @Column(name = "population", nullable = false)
    @NotNull(message = "Population cannot be null")
    @Positive(message = "Population must be greater than 0")
    private Integer population;
    
    @Column(name = "establishment_date")
    private LocalDateTime establishmentDate;
    
    @Column(name = "capital")
    private Boolean capital;
    
    @Column(name = "meters_above_sea_level")
    private Long metersAboveSeaLevel;
    
    @Column(name = "car_code")
    @Positive(message = "Car code must be greater than 0")
    @Min(value = 1, message = "Car code must be greater than 0")
    @Max(value = 1000, message = "Car code cannot exceed 1000")
    private Long carCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "climate")
    private Climate climate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "standard_of_living")
    private StandardOfLiving standardOfLiving;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "governor_id")
    private Human governor;
    
    // Constructors
    public City() {
        this.creationDate = LocalDateTime.now();
        this.coordinates = new Coordinates(0.0f, 0L);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Coordinates getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    public Integer getArea() {
        return area;
    }
    
    public void setArea(Integer area) {
        this.area = area;
    }
    
    public Integer getPopulation() {
        return population;
    }
    
    public void setPopulation(Integer population) {
        this.population = population;
    }
    
    public LocalDateTime getEstablishmentDate() {
        return establishmentDate;
    }
    
    public void setEstablishmentDate(LocalDateTime establishmentDate) {
        this.establishmentDate = establishmentDate;
    }
    
    public Boolean getCapital() {
        return capital;
    }
    
    public void setCapital(Boolean capital) {
        this.capital = capital;
    }
    
    public Long getMetersAboveSeaLevel() {
        return metersAboveSeaLevel;
    }
    
    public void setMetersAboveSeaLevel(Long metersAboveSeaLevel) {
        this.metersAboveSeaLevel = metersAboveSeaLevel;
    }
    
    public Long getCarCode() {
        return carCode;
    }
    
    public void setCarCode(Long carCode) {
        this.carCode = carCode;
    }
    
    public Climate getClimate() {
        return climate;
    }
    
    public void setClimate(Climate climate) {
        this.climate = climate;
    }
    
    public StandardOfLiving getStandardOfLiving() {
        return standardOfLiving;
    }
    
    public void setStandardOfLiving(StandardOfLiving standardOfLiving) {
        this.standardOfLiving = standardOfLiving;
    }
    
    public Human getGovernor() {
        return governor;
    }
    
    public void setGovernor(Human governor) {
        this.governor = governor;
    }
    
    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", area=" + area +
                ", population=" + population +
                ", establishmentDate=" + establishmentDate +
                ", capital=" + capital +
                ", metersAboveSeaLevel=" + metersAboveSeaLevel +
                ", carCode=" + carCode +
                ", climate=" + climate +
                ", standardOfLiving=" + standardOfLiving +
                ", governor=" + governor +
                '}';
    }
}
