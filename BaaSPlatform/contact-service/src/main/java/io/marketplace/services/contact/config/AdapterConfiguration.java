package io.marketplace.services.contact.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment-adapter")
@Data
public class AdapterConfiguration {
  private List<AdapterDefinition> mappings;
}
