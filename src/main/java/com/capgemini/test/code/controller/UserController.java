package com.capgemini.test.code.controller;

import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.dto.CreatedUserResponse;
import com.capgemini.test.code.dto.UserResponse;
import com.capgemini.test.code.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{id}")
  public UserResponse getUser(@PathVariable Long id) {
    return userService.getUserFromRoomOne(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CreatedUserResponse createUser(@RequestBody CreateUserRequest request) {
    return userService.createUser(request);
  }
}
