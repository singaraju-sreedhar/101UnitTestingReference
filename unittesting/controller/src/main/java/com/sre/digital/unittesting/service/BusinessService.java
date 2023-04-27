package com.sre.digital.unittesting.service;

import com.sre.digital.unittesting.model.Item;
import com.sre.digital.unittesting.repository.Itemrepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class BusinessService {

    private static final String CREATED_AT_COLUMN = "id";

    @Autowired
    RedisTemplate<String, Item> redisTemplate;
    @Autowired
    Itemrepository itemrepository;

    @Autowired
    RestTemplate restTemplate;


    public Item getDummyItem(String dummy)
    {
        return  new Item(1,"dummyname",(float)78.19,1000);
    }


    public String callExternalService()
    {
        String retVal=restTemplate.getForObject("http://localhost:49080/sampleendpoint",String.class);

        return retVal;
    }

    public List<Item> getFilteredDummyItem(int id)
    {
        List<Item> itemList = new ArrayList<>();
        List<Item> itemstoreturn=new ArrayList<>();

        itemList.add(new Item(1,"apple",(float)78.19,1000));
        itemList.add(new Item(2,"apple",(float)19,1000));
        itemList.add(new Item(3,"papaya",(float)27,1000));

        for (Item item : itemList) {
            if(item.getId()==id)
            {
                itemstoreturn.add(item);
                break;
            }
        }
        return itemstoreturn;
    }

    public  Pageable CreatePageable()
    {
        Sort.Direction direction = Sort.Direction.DESC;
        String fieldPassed = CREATED_AT_COLUMN;
        List<Sort.Order> sortOrders = new ArrayList<>();
        // sortOrders.add(new Sort.Order(direction, fieldPassed));
        return  PageRequest.of(0,10, Sort.by(sortOrders));
    }

    public List<Item> getRealItems()
    {
        List<Item> itemList=new ArrayList<>();


        Page<Item> itemPage= itemrepository.findAll(CreatePageable());
        itemPage.get().forEach(it->itemList.add(it));

        return  itemList;
    }

    public Item getFilteredRealItem(int id)
    {

        Item item=redisTemplate.opsForValue().get(Integer.toString(id));

        if(null==item)
        {
            item=itemrepository.findById(id).get();
            redisTemplate.opsForValue().set(Integer.toString(id),item);
            System.out.println("Redis Cache missed for id ="+id);
        }
        else
            System.out.println("Redis Cache hit and optimized read");

        return item;
    }
}
