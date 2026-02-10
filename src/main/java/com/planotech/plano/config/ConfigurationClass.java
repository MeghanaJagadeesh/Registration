package com.planotech.plano.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ConfigurationClass {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
