package io.marketplace.services.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDto {

    private String userId;
    private String serviceCode;
    private String subServiceCode;
    private String displayName;
    private String paymentReference;
    private String mobileNumber;
    private String accountNumber;
    private String branchCode;
    private String bankCode;
    private String city;
    private String state;
    private String postCode;
    private String address;
    private String verificationStatus;
    private LocalDateTime verificationAt;
}
