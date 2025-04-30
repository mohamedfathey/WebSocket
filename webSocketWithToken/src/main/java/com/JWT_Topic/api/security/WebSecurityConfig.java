package com.JWT_Topic.api.security;

import com.JWT_Topic.service.JWTService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // This is crucial
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    private final JWTService jwtService;

    public WebSecurityConfig(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c->c.disable())
                .cors(c->c.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/public/**","/chat/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JWTAuthenticationFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}