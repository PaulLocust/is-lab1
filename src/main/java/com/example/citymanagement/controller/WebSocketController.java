package com.example.citymanagement.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/city-update")
    @SendTo("/topic/city-updates")
    public String handleCityUpdate(String message) {
        return message;
    }
}
