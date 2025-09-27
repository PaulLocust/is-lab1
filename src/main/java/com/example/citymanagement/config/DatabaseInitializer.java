package com.example.citymanagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Create functions if they don't exist
        createFunctions();
    }

    private void createFunctions() {
        try {
            // Delete cities by climate function
            String deleteFunction = """
                CREATE OR REPLACE FUNCTION delete_cities_by_climate(climate_param VARCHAR(50))
                RETURNS INTEGER AS $$
                DECLARE
                    deleted_count INTEGER;
                BEGIN
                    DELETE FROM cities WHERE climate = climate_param;
                    GET DIAGNOSTICS deleted_count = ROW_COUNT;
                    RETURN deleted_count;
                END;
                $$ LANGUAGE plpgsql;
                """;
            jdbcTemplate.execute(deleteFunction);

            // Average meters above sea level function
            String avgFunction = """
                CREATE OR REPLACE FUNCTION get_average_meters_above_sea_level()
                RETURNS DECIMAL AS $$
                DECLARE
                    avg_meters DECIMAL;
                BEGIN
                    SELECT AVG(meters_above_sea_level) INTO avg_meters 
                    FROM cities 
                    WHERE meters_above_sea_level IS NOT NULL;
                    RETURN COALESCE(avg_meters, 0);
                END;
                $$ LANGUAGE plpgsql;
                """;
            jdbcTemplate.execute(avgFunction);

            // Unique car codes function
            String carCodesFunction = """
                CREATE OR REPLACE FUNCTION get_unique_car_codes()
                RETURNS TABLE(car_code BIGINT) AS $$
                BEGIN
                    RETURN QUERY
                    SELECT DISTINCT c.car_code 
                    FROM cities c 
                    WHERE c.car_code IS NOT NULL
                    ORDER BY c.car_code;
                END;
                $$ LANGUAGE plpgsql;
                """;
            jdbcTemplate.execute(carCodesFunction);

            // Distance to max area city function
            String distanceMaxAreaFunction = """
                CREATE OR REPLACE FUNCTION calculate_distance_to_max_area_city()
                RETURNS DECIMAL AS $$
                DECLARE
                    max_area_city RECORD;
                    distance DECIMAL;
                BEGIN
                    SELECT c.id, c.coordinates_id, c.meters_above_sea_level, co.x, co.y
                    INTO max_area_city
                    FROM cities c
                    JOIN coordinates co ON c.coordinates_id = co.id
                    WHERE c.area = (SELECT MAX(area) FROM cities)
                    LIMIT 1;
                    
                    IF max_area_city.id IS NOT NULL THEN
                        distance := SQRT(
                            POWER(max_area_city.x - 0, 2) + 
                            POWER(max_area_city.y - 0, 2) + 
                            POWER(COALESCE(max_area_city.meters_above_sea_level, 0) - 0, 2)
                        );
                    ELSE
                        distance := 0;
                    END IF;
                    
                    RETURN distance;
                END;
                $$ LANGUAGE plpgsql;
                """;
            jdbcTemplate.execute(distanceMaxAreaFunction);

            // Distance to max population city function
            String distanceMaxPopFunction = """
                CREATE OR REPLACE FUNCTION calculate_distance_to_max_population_city()
                RETURNS DECIMAL AS $$
                DECLARE
                    max_pop_city RECORD;
                    distance DECIMAL;
                BEGIN
                    SELECT c.id, c.coordinates_id, c.meters_above_sea_level, co.x, co.y
                    INTO max_pop_city
                    FROM cities c
                    JOIN coordinates co ON c.coordinates_id = co.id
                    WHERE c.population = (SELECT MAX(population) FROM cities)
                    LIMIT 1;
                    
                    IF max_pop_city.id IS NOT NULL THEN
                        distance := SQRT(
                            POWER(max_pop_city.x - 0, 2) + 
                            POWER(max_pop_city.y - 0, 2) + 
                            POWER(COALESCE(max_pop_city.meters_above_sea_level, 0) - 0, 2)
                        );
                    ELSE
                        distance := 0;
                    END IF;
                    
                    RETURN distance;
                END;
                $$ LANGUAGE plpgsql;
                """;
            jdbcTemplate.execute(distanceMaxPopFunction);

            System.out.println("Database functions created successfully");
        } catch (Exception e) {
            System.err.println("Error creating database functions: " + e.getMessage());
            // Don't fail the application startup if functions already exist
        }
    }
}
