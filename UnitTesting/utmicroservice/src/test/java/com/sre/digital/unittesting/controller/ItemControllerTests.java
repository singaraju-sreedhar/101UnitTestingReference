package com.sre.digital.unittesting.controller;

import com.sre.digital.unittesting.controller.ItemController;
import com.sre.digital.unittesting.model.Item;
import com.sre.digital.unittesting.service.BusinessService;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.DocumentContext.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ItemControllerTests {

	@Autowired
	public MockMvc mockMvc;

	@MockBean
	BusinessService businessService;

	@Test
	public  void ApiFuncTest() throws Exception {

		RequestBuilder request = MockMvcRequestBuilders
				.get("/apifunc")
				.accept(MediaType.APPLICATION_JSON);

		MvcResult mvcResult = mockMvc.perform(request).andExpect(status().isOk()).andReturn();

		assertEquals("apifunction without args", mvcResult.getResponse().getContentAsString());

	}

	@Test
	public void DummyTest() throws Exception {

		String expected_response = "{\"id\":1,\"name\":\"dummyname\",\"price\":78.99,\"quantity\":1000}";


		RequestBuilder request = MockMvcRequestBuilders
				.get("/dummyitem")
				.accept(MediaType.APPLICATION_JSON);

		mockMvc.perform(request).andExpect(status().isOk())
				.andExpect(content().json(expected_response));

	}

	@Test
	public void DummyItemFromServiceTestExpectToEmpty() throws Exception {
		//the result will be empty because we have mocked the BusinessService bean
		//and did not provide any mock implementation

		String expected_response = "{\"id\":1,\"name\":\"dummyname\",\"price\":78.99,\"quantity\":1000}";


		RequestBuilder request = MockMvcRequestBuilders
				.get("/itemfromservice")
				.accept(MediaType.APPLICATION_JSON);

		MvcResult response=mockMvc.perform(request).andExpect(status().isOk()).andReturn();

		System.out.println("Response received : "+response);

		assertThat(response.getResponse().getContentAsString()).isEmpty();

	}

	@Test
	public void FilteredDummyItemTest() throws Exception {

		List<Item> mockedResponse = new ArrayList<>();
		mockedResponse.add(new Item(100, "dummyname", (float) 78.97, 1000));

		when(businessService.getFilteredDummyItem(1)).thenReturn(mockedResponse);

		RequestBuilder request = MockMvcRequestBuilders
				.get("/dummyitemfilteredfromservice?id=1")
				.accept(MediaType.APPLICATION_JSON);

		MvcResult mvcResult = mockMvc.perform(request).andExpect(status().isOk()).andReturn();

		System.out.println("Returned data: " + mvcResult.getResponse().getContentAsString());
	}

	@Test
	public void DummyItemFromMockedServiceTest() throws Exception {

		String expected_response = "{\"id\":1,\"name\":\"dummyname\",\"price\":78.99,\"quantity\":1000}";

		Item mockedResponse = new Item(1, "dummyname", (float) 78.99, 1000);

		when(businessService.getDummyItem(null)).thenReturn(mockedResponse);

		RequestBuilder request = MockMvcRequestBuilders
				.get("/itemfromservice")
				.accept(MediaType.APPLICATION_JSON);

		mockMvc.perform(request).andExpect(status().isOk())
				.andExpect(content().json(expected_response));
	}


}
