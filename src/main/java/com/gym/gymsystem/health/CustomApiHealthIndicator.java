package com.gym.gymsystem.health;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomApiHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CustomApiHealthIndicator(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Health health() {
        String apiUrl = "http://localhost:8080/api/training-types";
        try {
            String response = restTemplate.getForObject(apiUrl, String.class);
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                if (jsonNode.isArray() && !jsonNode.isEmpty()) {
                    return Health.up().withDetail("API", "Available").build();
                }
            }
            return Health.down().withDetail("API", "Unavailable").build();
        } catch (Exception e) {
            return Health.down(e).withDetail("API", "Error occurred").build();
        }
    }
}
