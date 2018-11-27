package com.qi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.sidecar.EnableSidecar;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableSidecar  //使用@EnableSidecar整合非jvm应用@EnableSidecar整合了 @EnableCircuitBreaker @EnableZuulProxy
public class Zuul1Application {
	public static void main(String[] args) {
		SpringApplication.run(Zuul1Application.class, args);
	}
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}
}
