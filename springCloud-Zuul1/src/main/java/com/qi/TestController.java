package com.qi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
public class TestController {
	@Autowired
	private RestTemplate restTemplate;
	/**
	 *访问非jvm应用
	 */
	@GetMapping("/test{id}")
	public String findById(@PathVariable String id){
		return restTemplate.getForObject("http://springCloud-Zuul1:8080", String.class);
	}
}
