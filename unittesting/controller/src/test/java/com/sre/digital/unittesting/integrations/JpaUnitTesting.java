package com.sre.digital.unittesting.integrations;


import com.sre.digital.unittesting.model.Item;
import com.sre.digital.unittesting.repository.Itemrepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.assertj.core.api.Assertions.assertThat;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.DocumentContext.*;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class JpaUnitTesting {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private Itemrepository itemrepository;

    @Test
    public void testFindByUsername() {
        // given
        Item item = new Item();

        item.setId(78);
        item.setName("testjpaitem");
        item.setPrice((float)89.8);
        item.setQuantity(786);

        entityManager.persist(item);
        entityManager.flush();

        // when
        Optional<Item> found = itemrepository.findById(78);

        // then
        assertThat(found.get().getId())
                .isEqualTo(item.getId());
    }
}


