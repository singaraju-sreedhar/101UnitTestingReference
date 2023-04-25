package com.sre.digital.unittesting.service;

import com.sre.digital.unittesting.model.Item;
import com.sre.digital.unittesting.repository.Itemrepository;
import com.sre.digital.unittesting.service.BusinessService;
import com.sre.digital.unittesting.teststubs.TestStubs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

//@SpringBootTest
@WebMvcTest(BusinessService.class)
@ExtendWith(SpringExtension.class)
public class ServiceOnlyTest {

    @MockBean
    Itemrepository itemrepository;

    @Autowired
    BusinessService businessService;

    @Test
    void GetAllItemsUsingServiceLayerTest()
    {
        List<Item> itemList= Arrays.asList(
                            new Item(12,"banana",10,190),
                            new Item(13,"oranges",40,187));


        Page<Item> itemPage=new PageImpl<>(itemList, TestStubs.CreatePageable(),itemList.stream().count());

        when(itemrepository.findAll(TestStubs.CreatePageable())).thenReturn(itemPage);

        List<Item> actualItemlist=businessService.getRealItems();

        itemList.stream().forEach(item-> System.out.println(item.getId()+","+item.getName()));

        assertThat(actualItemlist).hasSize(2)
                .allMatch(actualit->itemList.contains(actualit));

    }
}
