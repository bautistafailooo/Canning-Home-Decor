package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.UserRequest;
import com.uade.tpo.demo.exceptions.UserNotFoundException;

public interface UserService {

    List<User> getUsers();

    Optional<User> getUserById(Long userId);

    User updateUser(Long userId, UserRequest userRequest) throws UserNotFoundException;

    void deleteUser(Long userId) throws UserNotFoundException;
}