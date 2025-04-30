package com.JWT_Topic.api.model;


import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotNull;

public class ResetPasswordRequest {

    @NotNull(message = "Email cannot be null")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotNull(message = "OTP cannot be null")
    @NotBlank(message = "OTP cannot be blank")
    private String otp;

    @NotNull(message = "Password cannot be null")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 32, message = "Password must be between 6 and 32 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$",
            message = "Password must contain at least one letter and one number")
    private String newPassword;

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
