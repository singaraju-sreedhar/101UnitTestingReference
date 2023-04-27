package io.marketplace.services.contact.service;

import io.marketplace.commons.model.ShortTermCache;
import io.marketplace.services.contact.adapters.MembershipAdapter;
import io.marketplace.services.contact.adapters.PaymentServiceAdapter;
import io.marketplace.services.contact.adapters.WalletServiceAdapter;
import io.marketplace.services.contact.entity.BeneficiaryEntity;
import io.marketplace.services.contact.mapper.BeneficiaryMapper;
import io.marketplace.services.contact.model.BeneficiaryResponse;
import io.marketplace.services.contact.repository.BeneficiaryRepository;
import io.marketplace.services.contact.specifications.BeneficiarySpecification;
import io.marketplace.services.contact.stubs.TestStubs;
import io.marketplace.services.contact.utils.AdapterUtils;
import io.marketplace.services.pxchange.client.service.EventServiceClient;
import io.marketplace.services.pxchange.client.service.PXChangeServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ContactService.class) //for beans referred in autowiring
@AutoConfigureMockMvc // this will instantiate local Mock objects
public class ContactServiceTests {

    @Autowired
    ContactService contactService;

    /**** mock transitive dependencies* */
    @MockBean
    BeneficiaryMapper beneficiaryMapper;
    @MockBean
    PXChangeServiceClient pxChangeServiceClient;

    @MockBean
    MembershipAdapter membershipAdapter;

    @MockBean
    WalletServiceAdapter walletServiceAdapter;

    @MockBean
    PaymentServiceAdapter paymentServiceAdapter;

    @MockBean
    ShortTermCache shortTermCache;

    @MockBean
    EventServiceClient eventServiceClient;

    @MockBean
    AdapterUtils adapterUtils;
    /************* end *******************/

    //this mock repository is added to Sprint Context
    //this means its gets added to ContaxtService too
    @MockBean
    BeneficiaryRepository beneficiaryRepository;

    @Test
    @DisplayName("Obtain All Contacts using ContactService Layer Only")
    void GetContactsContactServiceTest()
    {
        //Given
        String userID=null;
        String searchText=null;
        String bankCode=null;
        List<String> listOrders=null;
        Integer pageSize=10;
        Integer pageNum=1;

        Specification<BeneficiaryEntity> beneficiaryEntitySpecification =
                new BeneficiarySpecification(userID, searchText, bankCode);

        Pageable pageable=TestStubs.CreateMockPageable(pageNum,pageSize);


        Page<BeneficiaryEntity> beneficiaryEntityPage=
                                    TestStubs.CreateMockPageableBeneficiaryEntities(
                                                                pageNum,
                                                                pageSize,
                                                                pageable);

        //Page<BeneficiaryEntity> expectedPage = new PageImpl<>(Collections.emptyList());

        String loginID="";


        when(beneficiaryRepository
                .findAllByUserId(loginID,pageable))
                .thenReturn(beneficiaryEntityPage);

        //When service call is made
        BeneficiaryResponse beneficiaryResponse=contactService.getContactList(userID,
                                searchText,
                                bankCode,
                                pageSize,
                                pageNum,
                                listOrders);

        //Then perform verifications and Asserts
        System.out.println("Beneficiary Response from Service "+beneficiaryResponse.toString());
        assertThat(beneficiaryResponse.getData().size()).isEqualTo(1);
        assertThat(beneficiaryResponse.getData().get(0).getAccountNumber()).isEqualTo("1668649902518");
        assertThat(beneficiaryResponse.getData().get(0).getBankCode()).isEqualTo("ADB");

    }
}
