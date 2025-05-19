package org.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // Allow all requests
            .csrf().disable()     // Disable CSRF protection (safe for APIs)
            .formLogin().disable() // Disable default login page
            .httpBasic().disable(); // Disable HTTP Basic Auth
        return http.build();
    }
}
