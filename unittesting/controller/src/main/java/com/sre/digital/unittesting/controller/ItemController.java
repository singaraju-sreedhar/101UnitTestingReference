package com.sre.digital.unittesting.controller;

import com.sre.digital.unittesting.model.Item;
import com.sre.digital.unittesting.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@ComponentScan
public class ItemController {


    @Autowired
    BusinessService businessService;

    @GetMapping("/apifunc")
   public ResponseEntity<String> apiFunction()
    {
        return   ResponseEntity.status(HttpStatus.OK).body("apifunction without args");
    }

    @GetMapping("/dummyitem")
    public  ResponseEntity<Item> DummyItem(String dummyarg)
    {
        return ResponseEntity.status(HttpStatus.OK).body(
                new Item(1,"dummyname",(float)78.99,1000));
    }

    @GetMapping("/callexternal")
    public String CallExternal()
    {
        return  businessService.callExternalService();
    }

    @GetMapping("/itemfromservice")
    public ResponseEntity<Item> ItemFromService(String dummyarg)
    {
        Item item=businessService.getDummyItem(dummyarg);

        return  ResponseEntity.status(HttpStatus.OK).body(item);

    }

    @GetMapping("/dummyitemfilteredfromservice")
    public ResponseEntity<List<Item>> FilteredDummyItem(@RequestParam("id") Integer id)
    {
        return  ResponseEntity.status(HttpStatus.OK).body(businessService.getFilteredDummyItem(id));
    }

    @GetMapping("/itemfromsrvdb")
    public ResponseEntity<List<Item>> ItemFromServiceDB()
    {
        return  ResponseEntity.status(HttpStatus.OK).body(businessService.getRealItems());
    }

    @GetMapping("/filteritemfromsrvdb")
    public ResponseEntity<Item> FilterItemFromServiceDB(int id)
    {

        return  ResponseEntity.status(HttpStatus.OK)
                .body(businessService.getFilteredRealItem(id));
    }
}
