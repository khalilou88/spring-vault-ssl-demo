package com.demo.servera.service;

import com.demo.common.dto.MessageRequest;
import com.demo.common.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ServerAClientService {

    private static final Logger logger = LoggerFactory.getLogger(ServerAClientService.class);


    private final RestTemplate restTemplate;

    @Value("${app.server-b.url:https://localhost:8444}")
    private String serverBUrl;

    public ServerAClientService(RestTemplateBuilder restTemplateBuilder, SslBundles sslBundles) {

        this.restTemplate = restTemplateBuilder.setSslBundle(sslBundles.getBundle("server-a-ssl")).build();
    }


    public MessageResponse sendMessageToServerB(String message) {
        logger.info("Sending message to Server B: {}", message);

        MessageRequest request = new MessageRequest(message, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {

            MessageResponse response = restTemplate.postForObject(
                    serverBUrl + "/api/v1/message",
                    request,
                    MessageResponse.class
            );

            logger.info("Received response from Server B: {}", response.getResponse());
            return response;

        } catch (Exception e) {
            logger.error("Failed to communicate with Server B", e);
            return new MessageResponse("Failed to communicate with Server B: " + e.getMessage(), "server-a-error", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }
}