package io.marketplace.services.contact.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "beneficiary")
public class BeneficiaryEntity extends Auditable{

    @javax.persistence.Id
    @Id
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "service_code")
    private String serviceCode;

    @Column(name = "sub_service_code")
    private String subServiceCode;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "address")
    private String address;

    @Column(name = "verification_status")
    private String verificationStatus;

    @Column(name = "verification_at")
    private LocalDateTime verificationAt;
}
