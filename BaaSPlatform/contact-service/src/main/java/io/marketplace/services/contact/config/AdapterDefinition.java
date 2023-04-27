package io.marketplace.services.contact.config;

import lombok.Data;

import java.util.Map;

@Data
public class AdapterDefinition {
    private String adapterId;
    private String baseEndpoint;
    private String paymentReferenceScheme;
    private Map<String, String> mainTypeMapping;
    private Map<String, String> secondaryTypeMapping;
}
