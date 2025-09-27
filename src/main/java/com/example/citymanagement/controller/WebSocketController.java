package com.example.citymanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@Tag(name = "WebSocket Controller", description = "WebSocket контроллер для обработки обновлений городов в реальном времени")
public class WebSocketController {

    @Operation(summary = "Обработка обновления города", description = "Получает сообщение об обновлении города и рассылает его всем подписчикам")
    @MessageMapping("/city-update")
    @SendTo("/topic/city-updates")
    public String handleCityUpdate(String message) {
        return message;
    }
}