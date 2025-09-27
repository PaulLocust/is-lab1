package com.example.citymanagement.service;

import com.example.citymanagement.model.Coordinates;
import com.example.citymanagement.repository.CoordinatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CoordinatesService {
    
    @Autowired
    private CoordinatesRepository coordinatesRepository;
    
    public List<Coordinates> getAllCoordinates() {
        return coordinatesRepository.findAll();
    }
    
    public Optional<Coordinates> getCoordinatesById(Long id) {
        return coordinatesRepository.findById(id);
    }
    
    public Coordinates saveCoordinates(Coordinates coordinates) {
        return coordinatesRepository.save(coordinates);
    }
    
    public void deleteCoordinates(Long id) {
        coordinatesRepository.deleteById(id);
    }
}
