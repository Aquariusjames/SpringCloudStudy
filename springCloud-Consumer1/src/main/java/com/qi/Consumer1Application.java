package com.qi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.turbine.EnableTurbine;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableFeignClients //启用feign
@EnableEurekaClient
@EnableCircuitBreaker //开启Hystrix
@EnableHystrixDashboard //开启HystrixDashboard可视化监控 访问http://{ip}:serverPort/hystrix查看效果可看到HystrixDashboard主页 在URL栏输入http://{ip}:{port}/hystrix.stream title随便输入个名字点击monitor查看
@EnableTurbine //启动Turbine监控多个微服务 访问http://{ip}:serverPort/hystrix.stream在URL栏输入http://{ip}:{port}/turbine.stream
@EnableZuulProxy //启动Zuul网关 该代理使用Ribbon来定位注册在Eureka Server中的微服务；同时该代理还整合了Hystrix，从而实现了容错
public class Consumer1Application {
  @Bean
  @LoadBalanced //轮询容错
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  public static void main(String[] args) {
    SpringApplication.run(Consumer1Application.class, args);
  }
}
