package com.sre.digital.unittesting.integrations;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import com.sre.digital.unittesting.service.BusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class IntegrationUsingAPICallsFromServiceTests {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    BusinessService businessService;

    @Test
    public  void serviceRESTIntegrationTest()
    {
        //given
        String expectedResponse="Sample server endpoint";
        String url="http://localhost:49080/sampleendpoint";

        when(restTemplate.getForObject(url,String.class))
                .thenReturn(expectedResponse);

        //when
        String actualresponse=businessService.callExternalService();


        //then
        assertThat(actualresponse).isEqualTo(expectedResponse);
    }
}
