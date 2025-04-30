package com.JWT_Topic.api.controllerAuth;

import com.JWT_Topic.api.model.ApiResponse;
import com.JWT_Topic.api.model.LoginBody;
import com.JWT_Topic.api.model.RegistrationBody;
import com.JWT_Topic.api.model.ResetPasswordRequest;
import com.JWT_Topic.entity.User;
import com.JWT_Topic.exception.InvalidCredentialsException;
import com.JWT_Topic.exception.UserAlreadyExistsException;
import com.JWT_Topic.exception.UserNotFoundException;
import com.JWT_Topic.exception.UserNotVerifiedException;
import com.JWT_Topic.service.AuthService;
import com.JWT_Topic.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthenticationController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegistrationBody registrationBody) {
        try {
            User user = userService.registerUser(registrationBody);
            return ResponseEntity.ok(new ApiResponse(true, "Registration successful! Please check your email for the OTP.", user));
        } catch (UserAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An unexpected error occurred: " + ex.getMessage(), null));
        }
    }

    @PostMapping("/verify/otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        try {
            boolean verified = authService.verifyOtp(email, otp);
            if (verified) {
                return ResponseEntity.ok(new ApiResponse(true, "OTP verified successfully! Your email is now verified.", null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid or expired OTP.", null));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginMerchant(@RequestBody LoginBody loginBody) {
        try {
            String jwtUser = userService.loginUser(loginBody);
            return ResponseEntity.ok(new ApiResponse(true, "Login successful.", jwtUser));
        } catch (UserNotFoundException | InvalidCredentialsException | UserNotVerifiedException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
        try {
            authService.sendPasswordResetOtp(email);
            return ResponseEntity.ok(new ApiResponse(true, "OTP has been sent to your email.", null));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    @PostMapping("/verify/reset-otp")
    public ResponseEntity<ApiResponse> verifyResetOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = authService.verifyResetOtp(email, otp);
        if (isValid) {
            return ResponseEntity.ok(new ApiResponse(true, "OTP is valid.", null));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, "Invalid or expired OTP.", null));
    }

    @PostMapping("/reset/password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            boolean updated = authService.updatePasswordWithOtp(request.getEmail(), request.getOtp(), request.getNewPassword());
            if (updated) {
                return ResponseEntity.ok(new ApiResponse(true, "Password reset successfully.", null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid OTP or email.", null));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    @PostMapping("/resend/otp")
    public ResponseEntity<ApiResponse> resendOtp(@RequestParam String email) {
        try {
            authService.regenerateOtp(email);
            return ResponseEntity.ok(new ApiResponse(true, "A new OTP has been sent to your email.", null));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Failed to resend OTP: " + ex.getMessage(), null));
        }
    }
}