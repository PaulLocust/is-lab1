package com.example.citymanagement.repository;

import com.example.citymanagement.model.City;
import com.example.citymanagement.model.Climate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    
    @Query("SELECT c FROM City c WHERE c.name LIKE %:name%")
    List<City> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT c FROM City c WHERE c.climate = :climate")
    List<City> findByClimate(@Param("climate") Climate climate);
    
    @Query("SELECT c FROM City c ORDER BY c.area DESC")
    List<City> findAllOrderByAreaDesc();
    
    @Query("SELECT c FROM City c ORDER BY c.population DESC")
    List<City> findAllOrderByPopulationDesc();
    
    @Query(value = "SELECT get_average_meters_above_sea_level()", nativeQuery = true)
    Double getAverageMetersAboveSeaLevel();
    
    @Query(value = "SELECT car_code FROM get_unique_car_codes()", nativeQuery = true)
    List<Long> getUniqueCarCodes();
    
    @Query(value = "SELECT calculate_distance_to_max_area_city()", nativeQuery = true)
    Double calculateDistanceToMaxAreaCity();
    
    @Query(value = "SELECT calculate_distance_to_max_population_city()", nativeQuery = true)
    Double calculateDistanceToMaxPopulationCity();
    
    @Query(value = "SELECT delete_cities_by_climate(?1)", nativeQuery = true)
    Integer deleteCitiesByClimateFunction(@Param("climate") String climate);
    
    @Query("SELECT c FROM City c WHERE c.id IN (SELECT c2.id FROM City c2 WHERE c2.area = (SELECT MAX(c3.area) FROM City c3))")
    Optional<City> findCityWithMaxArea();
    
    @Query("SELECT c FROM City c WHERE c.id IN (SELECT c2.id FROM City c2 WHERE c2.population = (SELECT MAX(c3.population) FROM City c3))")
    Optional<City> findCityWithMaxPopulation();
    
    @Query("SELECT COUNT(c) FROM City c WHERE c.climate = :climate")
    long countByClimate(@Param("climate") Climate climate);
}
