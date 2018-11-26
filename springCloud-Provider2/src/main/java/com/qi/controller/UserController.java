package com.qi.controller;

import com.qi.entity.User;
import com.qi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
  @Autowired
  private UserRepository userRepository;

  @GetMapping("/{id}")
  public User findById(@PathVariable Long id) {
    User findOne = this.userRepository.getOne(id);
    return findOne;
  }
  @GetMapping("/get")
  public User get(User user) {
    return user;
  }

  @PostMapping("/post")
  public User post(@RequestBody User user) {
    return user;
  }
}
