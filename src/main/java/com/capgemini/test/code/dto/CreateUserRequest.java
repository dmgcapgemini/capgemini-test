package com.capgemini.test.code.dto;

public record CreateUserRequest(
    String name,
    String email,
    String phone,
    String rol,
    String dni) {
}
