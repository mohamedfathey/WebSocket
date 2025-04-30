package com.JWT_Topic.service;

//import com.ProjectGraduation.auth.entity.Merchant;
import com.JWT_Topic.entity.User;
import com.JWT_Topic.entity.repo.UserRepo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class JWTService {


    @Value("${jwt.algorithm.key}")
    private String algorithmKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiryInSeconds}")
    private int expiryInSeconds;

    private static final String USERNAME_KEY = "USERNAME";
    private Algorithm algorithm ;


    @PostConstruct
    public void postConstruct (){
        algorithm= Algorithm.HMAC256(algorithmKey) ;
    }


    public String generateJWTForUser(User user) {
        return JWT.create()
                .withClaim(USERNAME_KEY, user.getUsername())
                .withClaim("ROLE", user.getRole().toString()) // Add role to token
                .withClaim("USERNAME", user.getUsername()) // Add role to token// Add role to token
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * expiryInSeconds)))
                .withIssuer(issuer)
                .sign(algorithm);
    }

    public String getRole(String token) {
        return JWT.decode(token).getClaim("ROLE").asString(); // Extract role from token
    }

    public String getUsername(String token) {
        return JWT.decode(token).getClaim("USERNAME").asString();
    }

    @Autowired
    private UserRepo userRepository;


    public String getRoleByUsername(String username) {
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(username);
        if (optionalUser.isPresent()) {
            return optionalUser.get().getRole().toString(); // ممكن تكون "USER" أو "MERCHANT"
        }
        return "UNKNOWN";
    }




}
