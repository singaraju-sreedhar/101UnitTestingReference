package io.marketplace.services.contact.repository;

import io.marketplace.services.contact.entity.BeneficiaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryRepository extends JpaRepository<BeneficiaryEntity, UUID>, JpaSpecificationExecutor<BeneficiaryEntity> {

     List<BeneficiaryEntity> findAllByUserIdAndDisplayName(String userId, String displayName);
     Page<BeneficiaryEntity> findAllByUserId(String userId, Pageable pageable);
     List<BeneficiaryEntity> findAllByPaymentReferenceAndUserId(String paymentReference, String userId);
     List<BeneficiaryEntity> findAllByAccountNumberAndUserId(String accountNumber, String userId);
     Optional<BeneficiaryEntity> findByIdAndUserId(UUID fromString, String userId);
     List<BeneficiaryEntity> findAllByPaymentReferenceAndUserIdAndBankCode(String paymentReference, String userId, String bankCode);
     List<BeneficiaryEntity> findAllByAccountNumberAndUserIdAndBankCode(String accountNumber, String userId, String bankCode);
}
