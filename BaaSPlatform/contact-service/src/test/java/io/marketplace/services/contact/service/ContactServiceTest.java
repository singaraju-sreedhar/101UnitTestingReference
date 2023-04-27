package io.marketplace.services.contact.service;

import io.marketplace.commons.model.ShortTermCache;
import io.marketplace.services.contact.adapters.MembershipAdapter;
import io.marketplace.services.contact.adapters.PaymentServiceAdapter;
import io.marketplace.services.contact.adapters.WalletServiceAdapter;
import io.marketplace.services.contact.entity.BeneficiaryEntity;
import io.marketplace.services.contact.mapper.BeneficiaryMapper;
import io.marketplace.services.contact.model.BeneficiaryResponse;
import io.marketplace.services.contact.repository.BeneficiaryRepository;
import io.marketplace.services.contact.service.ContactService;
import io.marketplace.services.contact.specifications.BeneficiarySpecification;
import io.marketplace.services.contact.stubs.TestStubs;
import io.marketplace.services.contact.utils.AdapterUtils;
import io.marketplace.services.pxchange.client.service.EventServiceClient;
import io.marketplace.services.pxchange.client.service.PXChangeServiceClient;
import io.swagger.v3.oas.annotations.extensions.Extension;
import junit.extensions.TestSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static io.marketplace.services.contact.utils.Constants.CREATED_AT_COLUMN;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.DocumentContext.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc // this will instantiate local Mock objects
//@WebMvcTest(ContactService.class)
//@ExtendWith(SpringExtension.class)
//@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class,
 //       HibernateJpaAutoConfiguration.class })
public class ContactServiceTest {

    @Autowired
    ContactService contactService;

    /* these are created baesd on errors faced need to check as
     we will need to provide alternate implementation*/
   /* @MockBean
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
*/
    /***************************** till here******************/

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
