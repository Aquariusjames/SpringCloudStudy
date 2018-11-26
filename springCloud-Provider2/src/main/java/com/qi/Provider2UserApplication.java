package com.qi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class Provider2UserApplication {
  public static void main(String[] args) {
    SpringApplication.run(Provider2UserApplication.class, args);
  }
}
