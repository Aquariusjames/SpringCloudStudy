package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 使用Eureka做服务发现.
 * @author 周立
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication3 {
  public static void main(String[] args) {
    SpringApplication.run(EurekaApplication3.class, args);
  }
}
