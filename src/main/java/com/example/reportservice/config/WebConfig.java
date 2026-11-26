package com.example.reportservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebFlux configuration for non-blocking I/O.
 * All request handling is async with backpressure support.
 */
@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
