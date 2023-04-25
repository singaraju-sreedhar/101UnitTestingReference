package com.sre.digital.unittesting.repository;

import com.sre.digital.unittesting.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Itemrepository extends JpaRepository<Item, Integer> {

    Page<Item> findAll(Pageable pageable);
}
