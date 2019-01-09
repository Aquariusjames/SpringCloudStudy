package com.qi.adminClient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

//@EnableDiscoveryClient
@SpringBootApplication
public class AdminClientApplication {
	public static void main(String[] args) {
		SpringApplication.run(AdminClientApplication.class, args);
	}
}
