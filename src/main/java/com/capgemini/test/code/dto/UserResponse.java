package com.capgemini.test.code.dto;

public record UserResponse(
    Long id,
    String name,
    String email,
    String phone,
    String rol,
    String dni) {
}
