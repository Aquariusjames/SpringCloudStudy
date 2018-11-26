package com.qi.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.qi.entity.User;
import com.qi.feign.UserFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class MovieController {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovieController.class);
  @Autowired
  private RestTemplate restTemplate;

  private UserFeignClient userFeignClient;

  @Autowired
  private LoadBalancerClient loadBalancerClient;

  @Autowired
  private DiscoveryClient discoveryClient;

  @HystrixCommand(fallbackMethod = "findByIdFallback")
  @GetMapping("/user/{id}")
  public User findById(@PathVariable Long id) {
//    return this.restTemplate.getForObject("http://localhost:8001/" + id, User.class);
    return this.userFeignClient.findById(id);
  }

  public User findByIdFallback(Long id) {
    User user = new User();
    user.setId(-1L);
    user.setName("默认用户");
    return user;
  }

  @GetMapping("/log-user-instance")
  public void logUserInstance() {
    ServiceInstance serviceInstance = this.loadBalancerClient.choose("springCloud-Provider");
    // 打印当前选择的是哪个节点
    MovieController.LOGGER.info("{}:{}:{}", serviceInstance.getServiceId(), serviceInstance.getHost(), serviceInstance.getPort());
  }

  /**
   * 查询springCloud-Provider服务的信息并返回
   * @return springCloud-Provider服务的信息
   */
  @GetMapping("/user-instance")
  public List<ServiceInstance> showInfo() {
    return this.discoveryClient.getInstances("springCloud-Provider");
  }
}
