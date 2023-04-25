package com.sre.digital.unittesting;

import com.jayway.jsonpath.DocumentContext;
import com.sre.digital.unittesting.controller.ItemController;
import com.sre.digital.unittesting.model.Item;
import com.sre.digital.unittesting.repository.Itemrepository;
import com.sre.digital.unittesting.service.BusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.DocumentContext.*;


//@SpringBootTest
//@AutoConfigureMockMvc
//@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)


@WebMvcTest(ItemController.class)
public class WebOnlylayersTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    Itemrepository itemrepository;

    @MockBean
    BusinessService businessService;

    @Test
    void ItemsFromServiceDBTest() throws Exception {
        List<Item> mockedResponse = new ArrayList<>();
        mockedResponse.add(new Item(100, "dummyname", (float) 78.97, 1000));

       // when(itemrepository.findAll()).thenReturn(mockedResponse);
        when(businessService.getRealItems()).thenReturn(mockedResponse);

        RequestBuilder request= MockMvcRequestBuilders
                        .get("/itemfromsrvdb")
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(request).andExpect(status().isOk()).andReturn();

        System.out.println("Returned output : "+mvcResult.getResponse().getContentAsString());

        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        DocumentContext context= JsonPath.parse(mvcResult.getResponse().getContentAsString());

        int length=context.read("$.length()");

        assertThat(length).isEqualTo(1);
        assertThat(context.read("$.[0].id").toString()).isEqualTo("100");
        assertThat(context.read("$.[0].name").toString()).isEqualTo("dummyname");
        assertThat(context.read("$.[0].price").toString()).isEqualTo("78.97");
        assertThat(context.read("$.[0].quantity").toString()).isEqualTo("1000");

    }

}
