package io.marketplace.services.contact.controller;

import io.marketplace.services.contact.api.ContactsApiController;
import io.marketplace.services.contact.api.ContactsApiDelegate;
import io.marketplace.services.contact.model.BeneficiaryResponse;
import io.marketplace.services.contact.stubs.TestStubs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
@WebMvcTest(controllers = ContactsApiController.class)
@AutoConfigureMockMvc(addFilters = false) //for disabling security
public class ContactControllerTest {

    //use MockMvc to issue clls to the Controller in a stubbed HTTP mechanism.
    // This is not a real HTTP server based communication.
    @Autowired
    public MockMvc mockMvc;

    @MockBean
    ContactsApiDelegate contactsApiDelegate;

    //create appropriate test method
    @Test
    @DisplayName("Obtain All Contacts using Controller Layer Only")
    void getAllContactsTest() throws Exception {

        //The Implementation within a method suggested to follow Given-When-Then model

        //given ---- objects and states

        //mocked return data for the mock implementation
        BeneficiaryResponse beneficiaryResponse=TestStubs.CreateBenificiaryResponse();

        ResponseEntity<BeneficiaryResponse> resBeneficiaryResponse =
                ResponseEntity.ok().body(beneficiaryResponse);


        //input arguments
        String userID = null;
        String searchText = null;
        String bankCode = null;
        String listOrders = null;
        Integer pageSize = 10;
        Integer pageNum = 1;

        //this is the mock implementation provided when the getContactList is called on the
        //ContactDelegate Instance
        when(contactsApiDelegate.getContactList(
                userID,
                searchText,
                bankCode,
                listOrders,
                pageSize,
                pageNum)).thenReturn(resBeneficiaryResponse);


        //when --- the Test is performed on the System under test

        //issue the actual call using the simulated http stack
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/contacts")
                .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();


        //then --- conduct asserts and verifications


        //Conduct necessary verification of results

        System.out.println("returned value : " + mvcResult.getResponse().getContentAsString());


        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].identification").value("1e09d3e8-77c2-4da4-9dd5-042cfe934920"))
                .andExpect(jsonPath("$.data[0].bankCode").value("ADB"))
                .andExpect(jsonPath("$.data[0].displayName").value("Unit test display name"))
                .andExpect(jsonPath("$.data[0].accountNumber").value("1668649902518"))
                .andExpect(jsonPath("$.data[0].paymentReference").value(""));


    }
}