package io.marketplace.services.contact.service;

import io.marketplace.services.contact.model.BeneficiaryAccountDetails;
import io.marketplace.services.contact.model.BeneficiaryDataResponse;
import io.marketplace.services.contact.model.UpdateBeneficiaryRecord;

import org.springframework.stereotype.Service;

@Service
public class MockBeneficiaryService {

    public BeneficiaryAccountDetails updateBeneficiary(
            String type, String identification, UpdateBeneficiaryRecord updateBeneficiaryRecord) {
        BeneficiaryAccountDetails data = new BeneficiaryAccountDetails();
        data.setData(
                BeneficiaryDataResponse.builder()
                        .bankCode(updateBeneficiaryRecord.getBankCode())
                        .displayName(updateBeneficiaryRecord.getDisplayName())
                        .paymentReference(updateBeneficiaryRecord.getPaymentReference())
                        .identification(identification)
                        .secondaryIdentification(
                                updateBeneficiaryRecord.getSecondaryIdentification())
                        .type(type)
                        .secondaryType(updateBeneficiaryRecord.getSecondaryType())
                        .status("active")
                        .build());
        return data;
    }
}
