package com.sre.digital.unittesting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.sre.digital.unittesting.model"})
@EnableJpaRepositories(basePackages = {"com.sre.digital.unittesting.repository"})
public class ControllerTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ControllerTestApplication.class, args);
	}

}
