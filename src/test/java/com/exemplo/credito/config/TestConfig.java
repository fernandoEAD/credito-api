package com.exemplo.credito.config;

import com.exemplo.credito.service.AuditoriaPublisher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public AuditoriaPublisher auditoriaPublisher() {
        return mock(AuditoriaPublisher.class);
    }
} 