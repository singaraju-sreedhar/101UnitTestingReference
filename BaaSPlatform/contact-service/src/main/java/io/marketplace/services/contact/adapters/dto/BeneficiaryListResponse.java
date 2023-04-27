package io.marketplace.services.contact.adapters.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BeneficiaryListResponse {
    private List<BeneficiaryRecord> data;

    @Data
    @Builder
    public static class BeneficiaryRecord {

        private String paymentReference;

        private String displayName;

        private String bankCode;

        private String identification;

        private String type;

        private String status;
    }
}
