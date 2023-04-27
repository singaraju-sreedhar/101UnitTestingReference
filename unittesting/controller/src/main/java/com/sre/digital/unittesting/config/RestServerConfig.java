package com.sre.digital.unittesting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan
public class RestServerConfig
{

     RestTemplate restTemplate;

    @Bean
    public RestTemplate getRestTemplate() {
        restTemplate= new RestTemplate();
        return restTemplate;
    }
}
