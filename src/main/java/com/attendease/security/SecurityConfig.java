package com.attendease.security;

import com.attendease.repository.AccountRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService users(AccountRepository accounts) {
        return input -> {
            String login = input.strip();
            var account = (login.contains("@") ? accounts.findByEmailIgnoreCase(login)
                    : accounts.findByUsernameIgnoreCase(login))
                    .orElseThrow(() -> new UsernameNotFoundException("Account not found."));
            return User.withUsername(account.getId().toString()).password(account.getPasswordHash())
                    .roles(account.getRole().name()).disabled(!account.getActive()).build();
        };
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**", "/error").permitAll()
                        .requestMatchers("/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/", true))
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .build();
    }
}
