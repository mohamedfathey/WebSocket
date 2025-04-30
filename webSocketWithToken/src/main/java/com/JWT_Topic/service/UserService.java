package com.JWT_Topic.service;

import com.JWT_Topic.api.model.LoginBody;
import com.JWT_Topic.api.model.RegistrationBody;
import com.JWT_Topic.entity.User;
import com.JWT_Topic.entity.repo.UserRepo;
import com.JWT_Topic.exception.InvalidCredentialsException;
import com.JWT_Topic.exception.UserAlreadyExistsException;
import com.JWT_Topic.exception.UserNotFoundException;
import com.JWT_Topic.exception.UserNotVerifiedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private AuthService authService;

    public User registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException {
        if (userRepo.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + registrationBody.getEmail());
        }
        if (userRepo.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + registrationBody.getUsername());
        }

        User user = new User();
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setUsername(registrationBody.getUsername());
        user.setEmail(registrationBody.getEmail());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        user.setRole(registrationBody.getRole());

        userRepo.save(user);
        authService.generateAndSendOtp(user.getEmail());

        return user;
    }

    public String loginUser(LoginBody loginUserBody) {
        User user = userRepo.findByUsernameIgnoreCase(loginUserBody.getUsername())
                .orElseGet(() -> userRepo.findByEmailIgnoreCase(loginUserBody.getUsername())
                        .orElseThrow(() -> new InvalidCredentialsException("Invalid username or email")));

        if (!encryptionService.verifyPassword(loginUserBody.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        if (!user.isVerified()) {
            throw new UserNotVerifiedException("User email is not verified");
        }

        return jwtService.generateJWTForUser(user);
    }

    public User getUserByUsername(String username) {
        return userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }
}
