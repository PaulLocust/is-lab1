-- Create tables for City Management System

-- Coordinates table
CREATE TABLE IF NOT EXISTS coordinates (
    id BIGSERIAL PRIMARY KEY,
    x REAL NOT NULL,
    y BIGINT NOT NULL CHECK (y > -959)
);

-- Humans table
CREATE TABLE IF NOT EXISTS humans (
    id BIGSERIAL PRIMARY KEY,
    height REAL NOT NULL CHECK (height > 0)
);

-- Cities table
CREATE TABLE IF NOT EXISTS cities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL CHECK (LENGTH(name) > 0),
    coordinates_id BIGINT NOT NULL REFERENCES coordinates(id) ON DELETE CASCADE,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    area INTEGER NOT NULL CHECK (area > 0),
    population INTEGER NOT NULL CHECK (population > 0),
    establishment_date TIMESTAMP,
    capital BOOLEAN,
    meters_above_sea_level BIGINT,
    car_code BIGINT CHECK (car_code > 0 AND car_code <= 1000),
    climate VARCHAR(50) CHECK (climate IN ('RAIN_FOREST', 'TROPICAL_SAVANNA', 'OCEANIC')),
    standard_of_living VARCHAR(50) CHECK (standard_of_living IN ('HIGH', 'LOW', 'VERY_LOW')),
    governor_id BIGINT REFERENCES humans(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_cities_name ON cities(name);
CREATE INDEX IF NOT EXISTS idx_cities_climate ON cities(climate);
CREATE INDEX IF NOT EXISTS idx_cities_area ON cities(area);
CREATE INDEX IF NOT EXISTS idx_cities_population ON cities(population);
CREATE INDEX IF NOT EXISTS idx_cities_creation_date ON cities(creation_date);
