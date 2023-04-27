package io.marketplace.services.contact.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration Class
 */
@Configuration
//@EnableJpaAuditing
@EnableJpaRepositories({ "io.marketplace.services.contact" })
@EnableTransactionManagement
public class JpaAuditingConfig {

}
