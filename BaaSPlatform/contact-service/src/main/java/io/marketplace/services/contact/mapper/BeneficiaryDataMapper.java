package io.marketplace.services.contact.mapper;

import io.marketplace.services.contact.adapters.dto.BeneficiaryListResponse;
import io.marketplace.services.contact.model.Beneficiary;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BeneficiaryDataMapper {

    public List<Beneficiary> getBeneficiaryData(BeneficiaryListResponse beneficiaryList) {
        List<Beneficiary> beneficiaryData = new ArrayList<>();
        if (beneficiaryList.getData() != null && !beneficiaryList.getData().isEmpty()) {
            for (BeneficiaryListResponse.BeneficiaryRecord i : beneficiaryList.getData()) {
                Beneficiary beneficiary = new Beneficiary();
                beneficiary.setIdentification(i.getIdentification());
                beneficiary.setPaymentReference(i.getPaymentReference());
                beneficiary.setType(i.getType());
                beneficiary.setBankCode(i.getBankCode());
                beneficiary.setDisplayName(i.getDisplayName());
                beneficiary.setStatus(i.getStatus());
                beneficiaryData.add(beneficiary);
            }
        }
        return beneficiaryData;
    }
}
