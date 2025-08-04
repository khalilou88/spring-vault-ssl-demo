package com.demo.servera.service;

import com.demo.common.dto.MessageRequest;
import com.demo.common.dto.MessageResponse;
import com.demo.common.service.VaultSslService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ServerAClientService {

    private static final Logger logger = LoggerFactory.getLogger(ServerAClientService.class);

    private final VaultSslService vaultSslService;
    private final WebClient webClient;

    @Value("${app.server-b.url:https://localhost:8444}")
    private String serverBUrl;

    public ServerAClientService(VaultSslService vaultSslService) {
        this.vaultSslService = vaultSslService;
        this.webClient = createWebClient();
    }

    private WebClient createWebClient() {
        try {
            // Get SSL context from Vault for client authentication
            io.netty.handler.ssl.SslContext nettySslContext = vaultSslService.createNettySslContext("secret/data/ssl-certs/server-a");

            HttpClient httpClient = HttpClient.create().secure(sslSpec -> sslSpec.sslContext(nettySslContext));

            return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();

        } catch (Exception e) {
            logger.error("Failed to create SSL-enabled WebClient", e);
            // Fallback to non-SSL client for development
            return WebClient.builder().build();
        }
    }

    public MessageResponse sendMessageToServerB(String message) {
        logger.info("Sending message to Server B: {}", message);

        MessageRequest request = new MessageRequest(message, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            MessageResponse response = webClient.post().uri(serverBUrl + "/api/v1/message").bodyValue(request).retrieve().bodyToMono(MessageResponse.class).block();

            logger.info("Received response from Server B: {}", response.getResponse());
            return response;

        } catch (Exception e) {
            logger.error("Failed to communicate with Server B", e);
            return new MessageResponse("Failed to communicate with Server B: " + e.getMessage(), "server-a-error", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }
}