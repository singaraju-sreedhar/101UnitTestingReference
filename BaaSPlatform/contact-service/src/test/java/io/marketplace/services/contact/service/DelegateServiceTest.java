package io.marketplace.services.contact.service;

import io.marketplace.commons.utils.StringUtils;
import io.marketplace.services.contact.api.ContactsApiDelegate;
import io.marketplace.services.contact.api.delegate.ContactApiDelegateImpl;
import io.marketplace.services.contact.model.BeneficiaryResponse;
import io.marketplace.services.contact.stubs.TestStubs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.*;
import com.jayway.jsonpath.DocumentContext.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class DelegateServiceTest {

    //use MockMvc to issue clls to the Controller in a stubbed HTTP mechanism.
    // This is not a real HTTP server based communication.
    @Autowired
    MockMvc mockMvc;


   // @Autowired
    // ContactApiDelegateImpl contactApiDelegate;

    //this is the dependency service related class which we will be mocking
    @MockBean
    ContactService contactService;


    @Test
    @DisplayName("Obtain All Contacts using the Delegate Layer")
    void getContactListForAllContactsTest() throws Exception {

        //Given
        //initialization
        String userID=null;
        String searchText=null;
        String bankCode=null;
        String listOrders=null;
        Integer pageSize=10;
        Integer pageNum=1;
        List<String> _listOrders=null;

        //mock implementation
        BeneficiaryResponse beneficiaryResponse = TestStubs.CreateBenificiaryResponse();


        when(contactService.getContactList(userID,
                        searchText,
                        bankCode,
                        pageSize,
                        pageNum,
                StringUtils.stringToList(listOrders))).thenReturn(beneficiaryResponse);


        //when

        //Issue the actual call to the Controller+Delegate Implementation hook
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/contacts")
                .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult=mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();
      /* contacts=contactApiDelegate.getContactList(userID,
                            searchText,
                            bankCode,
                            listOrders,
                            pageSize,
                            pageNum);*/
        //then

        //Conduct verification
        String contacts=mvcResult.getResponse().getContentAsString();

        System.out.println("Data returned is "+contacts);

        DocumentContext documentContext=JsonPath.parse(contacts);


        int length=documentContext.read("$.data.length()");

        assertThat(length).isEqualTo(1);
        assertThat(documentContext.read("$.data[0].displayName").toString()).isEqualTo("Unit test display name");
        assertThat(documentContext.read("$.data[0].paymentReference").toString()).isEqualTo("");
        assertThat(documentContext.read("$.data[0].accountNumber").toString()).isEqualTo("1668649902518");
        assertThat(documentContext.read("$.data[0].bankCode").toString()).isEqualTo("ADB");
        assertThat(documentContext.read("$.data[0].identification").toString()).isEqualTo("1e09d3e8-77c2-4da4-9dd5-042cfe934920");

    }



}
