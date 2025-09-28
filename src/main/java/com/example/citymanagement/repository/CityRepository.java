package com.example.citymanagement.repository;

import com.example.citymanagement.model.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    @Query("SELECT c FROM City c WHERE c.name LIKE %:name%")
    Page<City> findByNameContaining(@Param("name") String name, Pageable pageable);

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

}
