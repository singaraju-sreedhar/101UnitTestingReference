package io.marketplace.services.contact.mapper;

import io.marketplace.commons.utils.StringUtils;
import io.marketplace.services.contact.entity.BeneficiaryEntity;
import io.marketplace.services.contact.model.Beneficiary;
import io.marketplace.services.contact.model.BeneficiaryRecord;
import io.marketplace.services.contact.model.Wallet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class BeneficiaryMapper {

    @Value("${contact.lookup.mask-account-number:false}")
    private Boolean maskAccountNumberFlag;

    @Value("${contact.lookup.mask-account-name:true}")
    private Boolean maskAccountNameFlag;

    public BeneficiaryEntity toBeneficiaryEntity(BeneficiaryRecord beneficiaryRecord, String userId) {

        return BeneficiaryEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .serviceCode(beneficiaryRecord.getServiceCode())
                .subServiceCode(beneficiaryRecord.getSubServiceCode())
                .displayName(beneficiaryRecord.getDisplayName())
                .paymentReference(beneficiaryRecord.getPaymentReference())
                .mobileNumber(beneficiaryRecord.getMobileNumber())
                .accountNumber(beneficiaryRecord.getAccountNumber())
                .branchCode(beneficiaryRecord.getBranchCode())
                .bankCode(beneficiaryRecord.getBankCode())
                .city(beneficiaryRecord.getCity())
                .state(beneficiaryRecord.getState())
                .postCode(beneficiaryRecord.getPostCode())
                .address(beneficiaryRecord.getAddress())
                .verificationStatus(
                        beneficiaryRecord.getVerificationStatus() != null ? beneficiaryRecord.getVerificationStatus()
                                : "")
                .address(beneficiaryRecord.getAddress())
                .build();
    }

    public List<Beneficiary> transformFromWalletDtoToBeneficiaryType(List<Wallet> walletDtos) {

        List<Beneficiary> beneficiaryDtoList = new ArrayList<>();

        for (Wallet walletObject : walletDtos) {
            String accountNumber = walletObject.getBankAccount().getAccountNumber();
            String maskAccountNumber = maskAccountNumberFlag && StringUtils.isNotEmpty(accountNumber)
                    && accountNumber.length() > 4
                            ? maskString(accountNumber, 0, accountNumber.length() - 4, 'x')
                            : accountNumber;

            String displayName = walletObject.getBankAccount().getAccountHolderName();
            String maskDisplayName = maskAccountNameFlag && StringUtils.isNotEmpty(displayName) ? maskName(displayName)
                    : displayName;
            if(walletObject.getIsDefaultWallet() != null && walletObject.getIsDefaultWallet()) {
                beneficiaryDtoList.add(Beneficiary.builder()
                        .accountNumber(maskAccountNumber)
                        .displayName(maskDisplayName)
                        .paymentReference(walletObject.getBankAccount().getAccountId())
                        .accountType(walletObject.getType().getValue())
                        .build());
            }
        }

        return beneficiaryDtoList;
    }

    public Beneficiary transformFromBeneficiaryRecordToBeneficiaryDto(BeneficiaryRecord beneficiaryRecord) {

        return Beneficiary.builder()
                .accountNumber(beneficiaryRecord.getAccountNumber())
                .displayName(beneficiaryRecord.getDisplayName())
                .paymentReference(beneficiaryRecord.getPaymentReference())
                .bankCode(beneficiaryRecord.getBankCode())
                .build();

    }

    private String maskName(final String fullName) {
        String[] arrName = fullName.split(" ");
        StringBuilder sbName = new StringBuilder();
        int index = 0;
        for (String nameItem : arrName) {
            if (index == 0) {
                sbName.append(nameItem);
            } else if (nameItem.length() >= 1) {
                sbName.append(nameItem.substring(0, 1).toUpperCase() + ".");
            }
            sbName.append(" ");
            index++;
        }
        return sbName.toString().trim();
    }

    private String maskString(String strText, int start, int end, char maskChar) {

        if (strText == null || strText.equals("")) {
            return "";
        }

        if (start < 0) {
            start = 0;
        }

        if (end > strText.length()) {
            end = strText.length();
        }

        if (start > end) {
            return strText;
        }

        int maskLength = end - start;

        if (maskLength == 0) {
            return strText;
        }

        StringBuilder sbMaskString = new StringBuilder(maskLength);

        for (int i = 0; i < maskLength; i++) {
            sbMaskString.append(maskChar);
        }

        return strText.substring(0, start)
                + sbMaskString.toString()
                + strText.substring(start + maskLength);
    }
}
