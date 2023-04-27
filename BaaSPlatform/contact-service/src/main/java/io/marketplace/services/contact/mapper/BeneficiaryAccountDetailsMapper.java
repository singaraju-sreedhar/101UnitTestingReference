package io.marketplace.services.contact.mapper;

import io.marketplace.services.contact.dto.BeneficiaryUpdateResponse;
import io.marketplace.services.contact.model.BeneficiaryAccountDetails;
import io.marketplace.services.contact.model.BeneficiaryDataResponse;

import org.springframework.stereotype.Component;

@Component
public class BeneficiaryAccountDetailsMapper {

    public BeneficiaryAccountDetails responseMapper(
            BeneficiaryUpdateResponse.BeneficiaryData beneficiaryData) {
        BeneficiaryAccountDetails data = new BeneficiaryAccountDetails();
        data.setData(
                BeneficiaryDataResponse.builder()
                        .bankCode(beneficiaryData.getBankCode())
                        .displayName(beneficiaryData.getDisplayName())
                        .paymentReference(beneficiaryData.getPaymentReference())
                        .identification(beneficiaryData.getIdentification())
                        .secondaryIdentification(beneficiaryData.getSecondaryIdentification())
                        .type(beneficiaryData.getType())
                        .secondaryType(beneficiaryData.getSecondaryType())
                        .status(beneficiaryData.getStatus())
                        .build());
        return data;
    }
}
