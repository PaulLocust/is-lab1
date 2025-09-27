package com.example.citymanagement.service;

import com.example.citymanagement.model.Human;
import com.example.citymanagement.repository.HumanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HumanService {
    
    @Autowired
    private HumanRepository humanRepository;
    
    public List<Human> getAllHumans() {
        return humanRepository.findAll();
    }
    
    public Optional<Human> getHumanById(Long id) {
        return humanRepository.findById(id);
    }
    
    public Human saveHuman(Human human) {
        return humanRepository.save(human);
    }
    
    public void deleteHuman(Long id) {
        humanRepository.deleteById(id);
    }
}
