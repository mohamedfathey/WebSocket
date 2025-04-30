package com.JWT_Topic.service;

import com.JWT_Topic.entity.User;
import com.JWT_Topic.entity.repo.UserRepo;
import com.JWT_Topic.exception.OtpStillValidException;
import com.JWT_Topic.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final int OTP_LENGTH = 6;

    @Autowired
    private final UserRepo repo;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EncryptionService encryptionService;

    private final Random random = new Random();

    public AuthService(UserRepo repo) {
        this.repo = repo;
    }

    private String generateOtp() {
        return String.format("%0" + OTP_LENGTH + "d", random.nextInt((int) Math.pow(10, OTP_LENGTH)));
    }

    private void sendOtpEmail(String email, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("mf7373057@gmail.com");
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void generateAndSendOtp(String email) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        repo.save(user);

        sendOtpEmail(email, "Your OTP Code", "Your OTP code is: " + otp + "\nIt expires in " + OTP_EXPIRATION_MINUTES + " minutes.");
    }

    public boolean verifyOtp(String email, String otp) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getOtp() == null || user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (!user.getOtp().equals(otp)) {
            return false;
        }

        user.setVerified(true);
        user.setOtp(null);
        repo.save(user);
        return true;
    }

    public void sendPasswordResetOtp(String email) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Email not found: " + email));

        String otp = generateOtp();
        user.setResetOtp(otp);
        user.setResetOtpExpiration(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        repo.save(user);

        sendOtpEmail(email, "Password Reset OTP", "Your OTP for password reset is: " + otp + "\nExpires in " + OTP_EXPIRATION_MINUTES + " minutes.");
    }

    public boolean verifyResetOtp(String email, String otp) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getResetOtp() == null || user.getResetOtpExpiration().isBefore(LocalDateTime.now())) {
            return false;
        }

        return user.getResetOtp().equals(otp);
    }

    public boolean updatePasswordWithOtp(String email, String otp, String newPassword) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getResetOtp() == null || user.getResetOtpExpiration().isBefore(LocalDateTime.now()) || !user.getResetOtp().equals(otp)) {
            return false;
        }

        user.setPassword(encryptionService.encryptPassword(newPassword));
        user.setResetOtp(null);
        user.setResetOtpExpiration(null);
        repo.save(user);
        return true;
    }

    public void regenerateOtp(String email) {
        User user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getOtpExpiration() != null && user.getOtpExpiration().isAfter(LocalDateTime.now())) {
            throw new OtpStillValidException("Current OTP is still valid until " + user.getOtpExpiration());
        }

        String newOtp = generateOtp();
        user.setOtp(newOtp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        repo.save(user);

        sendOtpEmail(email, "New OTP Code", "Your new OTP code is: " + newOtp + "\nIt expires in " + OTP_EXPIRATION_MINUTES + " minutes.");
    }
}