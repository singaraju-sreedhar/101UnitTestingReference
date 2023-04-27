package io.marketplace.services.contact;

import io.marketplace.services.contact.model.BeneficiaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ComponentE2ETest
{
    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    @DisplayName("Obtain All Contacts using all Layers")
    void GetContactsUsingAllLayersTest()
    {
        BeneficiaryResponse beneficiaryResponse=
                        testRestTemplate.getForObject("/contacts",BeneficiaryResponse.class);

        System.out.println("E2E result "+beneficiaryResponse);


    }
}
