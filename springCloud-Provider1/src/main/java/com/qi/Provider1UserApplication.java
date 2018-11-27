package com.qi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import zipkin2.server.internal.EnableZipkinServer;

@EnableDiscoveryClient
@SpringBootApplication
@EnableZipkinServer  //开启Zipkin 访问http://localhost:8001查看效果
public class Provider1UserApplication {
  public static void main(String[] args) {
    SpringApplication.run(Provider1UserApplication.class, args);
  }
}
