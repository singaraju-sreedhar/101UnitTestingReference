package io.marketplace.services.contact.stubs;

import io.marketplace.services.contact.entity.BeneficiaryEntity;
import io.marketplace.services.contact.model.BeneficiaryResponse;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.marketplace.services.contact.utils.Constants.CREATED_AT_COLUMN;

public class TestStubs {


    public static  Pageable CreateMockPageable(Integer pageNum, Integer pageSize)
    {
        Sort.Direction direction = Sort.Direction.DESC;
        String fieldPassed = CREATED_AT_COLUMN;
        List<Sort.Order> sortOrders = new ArrayList<>();
       // sortOrders.add(new Sort.Order(direction, fieldPassed));
        return  PageRequest.of((pageNum - 1), pageSize, Sort.by(sortOrders));
    }
public static Page<BeneficiaryEntity>  CreateMockPageableBeneficiaryEntities(
                                                Integer pageNum, Integer pageSize,
                                                Pageable pageable)
    {

        BeneficiaryEntity mockBeneficiaryEntity=new BeneficiaryEntity();
        mockBeneficiaryEntity.setId(UUID.randomUUID());
        mockBeneficiaryEntity.setUserId("1e09d3e8-77c2-4da4-9dd5-042cfe934920");
        mockBeneficiaryEntity.setBankCode("ADB");
        mockBeneficiaryEntity.setDisplayName("Unit test display name");
        mockBeneficiaryEntity.setAccountNumber("1668649902518");
        mockBeneficiaryEntity.setCreatedAt(LocalDateTime.now());
        mockBeneficiaryEntity.setUpdatedAt(LocalDateTime.now());
        List<BeneficiaryEntity> beneficiaries = new ArrayList<>();

        beneficiaries.add(mockBeneficiaryEntity);

        Page<BeneficiaryEntity> page = new PageImpl<>(beneficiaries, pageable, beneficiaries.size());

        return page;
    }

    public static BeneficiaryResponse CreateBenificiaryResponse() {

        io.marketplace.services.contact.model.Beneficiary beneficiary =
                new io.marketplace.services.contact.model.Beneficiary();

        beneficiary.setBankCode("ADB");
        beneficiary.setIdentification("1e09d3e8-77c2-4da4-9dd5-042cfe934920");
        beneficiary.setDisplayName("Unit test display name");
        beneficiary.setPaymentReference("");
        beneficiary.setAccountNumber("1668649902518");

        BeneficiaryResponse beneficiaryResponse = new BeneficiaryResponse();
        beneficiaryResponse.addDataItem(beneficiary);

        return  beneficiaryResponse;
    }
}
