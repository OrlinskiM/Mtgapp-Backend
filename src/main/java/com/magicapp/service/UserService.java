package com.magicapp.service;

import com.magicapp.domain.User;
import com.magicapp.exception.domain.EmailExistException;
import com.magicapp.exception.domain.UserNotFoundException;
import com.magicapp.exception.domain.UsernameExistException;

import java.util.List;

public interface UserService {

    User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException;

    List<User> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);
}
