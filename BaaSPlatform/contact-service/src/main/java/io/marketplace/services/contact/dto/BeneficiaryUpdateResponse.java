package io.marketplace.services.contact.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryUpdateResponse {

    private BeneficiaryData data;

    @Data
    @Builder
    public static class BeneficiaryData {

        private String paymentReference;

        private String displayName;

        private String bankCode;

        private String identification;

        private String type;

        private String secondaryType;

        private String secondaryIdentification;

        private String status;
    }
}
