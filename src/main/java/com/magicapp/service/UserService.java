package com.magicapp.service;

import com.magicapp.domain.User;
import com.magicapp.exception.domain.EmailExistException;
import com.magicapp.exception.domain.EmailNotFoundException;
import com.magicapp.exception.domain.UserNotFoundException;
import com.magicapp.exception.domain.UsernameExistException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {

    User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException;

    List<User> getUsers();

    User findUserByUsername(String username) throws UserNotFoundException;

    User findUserByEmail(String email);

    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException;

    User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException;

    void deleteUser(String username) throws IOException;
    void resetPassword(String email) throws MessagingException, EmailNotFoundException;

    User updateProfilePicture(String username, MultipartFile profileImage) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException;

    User findUserByUserId(Long userId);
}
