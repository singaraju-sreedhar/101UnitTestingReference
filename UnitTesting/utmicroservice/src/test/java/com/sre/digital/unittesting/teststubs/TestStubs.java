package com.sre.digital.unittesting.teststubs;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class TestStubs {
    private static final String CREATED_AT_COLUMN = "id";

    public static Pageable CreatePageable()
{
    Sort.Direction direction = Sort.Direction.DESC;
    String fieldPassed = CREATED_AT_COLUMN;
    List<Sort.Order> sortOrders = new ArrayList<>();
    // sortOrders.add(new Sort.Order(direction, fieldPassed));
    return  PageRequest.of(0,10, Sort.by(sortOrders));
}
}
