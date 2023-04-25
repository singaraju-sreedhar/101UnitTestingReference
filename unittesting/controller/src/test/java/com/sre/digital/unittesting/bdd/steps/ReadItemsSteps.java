package com.sre.digital.unittesting.bdd.steps;

import com.sre.digital.unittesting.model.Item;
import com.sre.digital.unittesting.repository.Itemrepository;
import com.sre.digital.unittesting.service.BusinessService;
import com.sre.digital.unittesting.teststubs.TestStubs;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class ReadItemsSteps {

    @MockBean
    Itemrepository itemrepository;

    @Autowired
    BusinessService businessService;

    List<Item> actualItemList=null;

    List<Item> expectedItems=null;


    @Given("items exists")
    public void itemsExists() {

        expectedItems=Arrays.asList(
                new Item(12,"banana",10,190),
                new Item(13,"oranges",40,187));

        Page<Item> itemPage=new PageImpl<>(expectedItems,
                            TestStubs.CreatePageable(),
                            expectedItems.stream().count());

        when(itemrepository.findAll(TestStubs.CreatePageable())).thenReturn(itemPage);

    }

    @When("items are read")
    public void itemsAreRead() {

        actualItemList = businessService.getRealItems();

    }

    @Then("the items are returned")
    public void theItemsAreReturned() {

        assertThat(actualItemList).hasSize(2)
                .allMatch(item -> expectedItems.contains(item) );

    }
}
