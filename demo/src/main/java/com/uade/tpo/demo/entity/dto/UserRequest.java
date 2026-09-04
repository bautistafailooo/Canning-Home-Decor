package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.Role;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String name;
    private String surname;
    private Role role;
}