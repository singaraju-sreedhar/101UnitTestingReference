package io.marketplace.services.contact;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.marketplace.commons.logging.EventCategory;
import io.marketplace.commons.logging.LogData;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
//@EnableJpaAuditing
public class ContactServiceApplication {
	
	 private static final Logger log = LoggerFactory.getLogger(ContactServiceApplication.class);

	 @Value("${spring.application.name}")
	 private static String applicationName;
	 
    public static void main(String[] args) {
        SpringApplication.run(ContactServiceApplication.class, args);
        log.trace(LogData.builder()
                .category(EventCategory.APPLICATION)                
                .title(applicationName + " successfully started")
                .build());
    }

    @PostConstruct
    private void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
   
}
