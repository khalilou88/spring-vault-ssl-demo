package com.demo.serverb.controller;

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
public class ServerBController {

    private static final Logger logger = LoggerFactory.getLogger(ServerBController.class);

    @PostMapping("/message")
    public ResponseEntity<MessageResponse> receiveMessage(@RequestBody MessageRequest request) {
        logger.info("Received message from Server A: {}", request.getMessage());

        MessageResponse response = new MessageResponse("Hello from Server B! Processing: " + request.getMessage(), "server-b", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Server B is healthy");
    }

    @GetMapping("/ssl-info")
    public ResponseEntity<String> sslInfo() {
        return ResponseEntity.ok("Server B running on HTTPS with Vault-managed certificates");
    }
}