package com.qi.controller;

import com.qi.entity.User;
import com.qi.feign.UserFeignClient;
import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Import(FeignClientsConfiguration.class)
@RestController
public class MovieControllerDecoder {
  @Autowired
  private RestTemplate restTemplate;

  private UserFeignClient userFeignClient;

  private UserFeignClient userUserFeignClient;

  private UserFeignClient adminUserFeignClient;

  /*@GetMapping("/user/{id}")
  public User findById(@PathVariable Long id) {
//    return this.restTemplate.getForObject("http://localhost:8001/" + id, User.class);
    return this.userFeignClient.findById(id);
  }
*/
  @Autowired
  public MovieControllerDecoder(Decoder decoder, Encoder encoder, Client client, Contract contract) {
    // 这边的decoder、encoder、client、contract，可以debug看看是什么实例。
    this.userUserFeignClient = Feign.builder().client(client).encoder(encoder).decoder(decoder).contract(contract)
            .requestInterceptor(new BasicAuthRequestInterceptor("user", "password1")).target(UserFeignClient.class, "http://springCloud-Provider/auth/");
    this.adminUserFeignClient = Feign.builder().client(client).encoder(encoder).decoder(decoder).contract(contract)
            .requestInterceptor(new BasicAuthRequestInterceptor("admin", "password2"))
            .target(UserFeignClient.class, "http://springCloud-Provider/auth/");
  }

  @GetMapping("/user-user/{id}")
  public User findByIdUser(@PathVariable Long id) {
    return this.userUserFeignClient.findById(id);
  }

  @GetMapping("/user-admin/{id}")
  public User findByIdAdmin(@PathVariable Long id) {
    return this.adminUserFeignClient.findById(id);
  }
}
