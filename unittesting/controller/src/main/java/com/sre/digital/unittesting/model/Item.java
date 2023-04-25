package com.sre.digital.unittesting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ITEM")
public class Item {

    @Id
    int id;
    String name;

    float price;

    long quantity;



}
