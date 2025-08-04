package com.demo.servera.controller;

import com.demo.common.dto.MessageRequest;
import com.demo.common.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1")
public class ServerAController {

    private static final Logger logger = LoggerFactory.getLogger(ServerAController.class);

    @PostMapping("/message")
    public ResponseEntity<MessageResponse> receiveMessage(@RequestBody MessageRequest request) {
        logger.info("Received message from client: {}", request.getMessage());

        MessageResponse response = new MessageResponse("Hello from Server A! Received: " + request.getMessage(), "server-a", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Server A is healthy");
    }

    @GetMapping("/ssl-info")
    public ResponseEntity<String> sslInfo() {
        return ResponseEntity.ok("Server A running on HTTPS with Vault-managed certificates");
    }
}