package com.SmarTrip.smarTrip_backend.Config;

import com.amadeus.Amadeus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmadeusConfig {

    @Value("${amadeus.api.key}")
    private String apiKey;

    @Value("${amadeus.api.secret}")
    private String apiSecret;

    @Bean
    public Amadeus amadeus() {
        return Amadeus.builder(apiKey, apiSecret)
                .setLogLevel("debug") // Set to debug during development
                .build();
    }
}