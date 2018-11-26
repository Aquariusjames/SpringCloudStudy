package com.qi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class Provider1UserApplication {
  public static void main(String[] args) {
    SpringApplication.run(Provider1UserApplication.class, args);
  }
}
