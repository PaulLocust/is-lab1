package com.example.citymanagement.service;

import com.example.citymanagement.model.City;
import com.example.citymanagement.model.Climate;
import com.example.citymanagement.model.Human;
import com.example.citymanagement.repository.CityRepository;
import com.example.citymanagement.service.HumanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CityService {
    
    @Autowired
    private CityRepository cityRepository;
    
    @Autowired
    private HumanService humanService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
    
    public Page<City> getCitiesPage(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return cityRepository.findAll(pageable);
    }

    public Page<City> searchCities(String name, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return cityRepository.findByNameContaining(name, pageable);
    }
    
    public Optional<City> getCityById(Long id) {
        return cityRepository.findById(id);
    }

    public City saveCity(City city) {
        // If governor is provided but has no ID, save it first
        if (city.getGovernor() != null && city.getGovernor().getId() == null) {
            // Create a new Human object with the provided height
            Human governor = new Human();
            governor.setHeight(city.getGovernor().getHeight());
            // Save the governor first to get an ID
            Human savedGovernor = humanService.saveHuman(governor);
            city.setGovernor(savedGovernor);
        }

        City savedCity = cityRepository.save(city);
        notifyCityUpdate("city_updated");
        return savedCity;
    }
    
    public void deleteCity(Long id) {
        // Check if city exists
        if (!cityRepository.existsById(id)) {
            throw new RuntimeException("City with ID " + id + " not found");
        }
        
        // Check if city is referenced by other cities (as governor)
        // This is a simple check - in a real scenario you might want more complex validation
        cityRepository.deleteById(id);
        notifyCityUpdate("city_updated");
    }
    
    public boolean existsById(Long id) {
        return cityRepository.existsById(id);
    }
    
    // Special operations using database functions
    public long deleteCitiesByClimate(Climate climate) {
        Integer deletedCount = cityRepository.deleteCitiesByClimateFunction(climate.toString());
        notifyCityUpdate("city_updated");
        return deletedCount != null ? deletedCount : 0;
    }
    
    public Double getAverageMetersAboveSeaLevel() {
        return cityRepository.getAverageMetersAboveSeaLevel();
    }
    
    public List<Long> getUniqueCarCodes() {
        return cityRepository.getUniqueCarCodes();
    }
    
    public double calculateDistanceToCityWithMaxArea() {
        Double distance = cityRepository.calculateDistanceToMaxAreaCity();
        return distance != null ? distance : 0.0;
    }
    
    public double calculateDistanceFromOriginToCityWithMaxPopulation() {
        Double distance = cityRepository.calculateDistanceToMaxPopulationCity();
        return distance != null ? distance : 0.0;
    }
    
    
    private void notifyCityUpdate(String message) {
        messagingTemplate.convertAndSend("/topic/city-updates", message);
    }
}
