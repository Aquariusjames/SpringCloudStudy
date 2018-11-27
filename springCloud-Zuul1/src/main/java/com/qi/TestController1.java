package com.qi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestController1 {
	@Autowired
	private RestTemplate restTemplate;

}
