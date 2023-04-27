package com.sre.sample.sampleservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @GetMapping("/sampleendpoint")
    String SampleEndpoint()
    {
        return "Sample server endpoint";
    }
}
