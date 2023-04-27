package com.sre.digital.unittesting.integrations;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import com.sre.digital.unittesting.service.BusinessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class IntegrationUsingAPICallsFromServiceTesting {

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

    public void serviceIntegratingWithOtherSeriveUsingAPITest() {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        String requestBody = "{\"name\":\"John\",\"age\":30}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        String expectedResponse = "{\"message\":\"Hello, John!\"}";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(expectedResponse);

        when(restTemplate.exchange("http://example.com/api/endpoint", HttpMethod.POST, requestEntity, String.class))
                .thenReturn(responseEntity);

        BusinessService myService = new BusinessService();

        myService.callExternalService();

        //tbd
        //add verification
    }
}
