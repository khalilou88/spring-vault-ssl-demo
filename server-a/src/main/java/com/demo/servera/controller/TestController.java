package com.demo.servera.controller;

import com.demo.common.dto.MessageResponse;
import com.demo.servera.service.ServerAClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final ServerAClientService clientService;

    public TestController(ServerAClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/send-to-server-b")
    public ResponseEntity<MessageResponse> testCommunication(@RequestParam String message) {
        MessageResponse response = clientService.sendMessageToServerB(message);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping-server-b")
    public ResponseEntity<MessageResponse> pingServerB() {
        MessageResponse response = clientService.sendMessageToServerB("Ping from Server A");
        return ResponseEntity.ok(response);
    }
}