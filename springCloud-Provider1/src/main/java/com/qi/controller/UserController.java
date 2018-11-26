package com.qi.controller;

import com.qi.entity.User;
import com.qi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class UserController {

//  @Value("${profile}")
  private String profile;

  @Autowired
  private UserRepository userRepository;

  @GetMapping("/profile")
  public String hello(){
    return this.profile;
  }

  @GetMapping("/{id}")
  public User findById(@PathVariable Long id) {
    User findOne = this.userRepository.getOne(id);
    return findOne;
  }
}
