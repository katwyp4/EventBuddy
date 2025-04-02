package com.kompetencyjny.EventBuddySpring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/auth/register").permitAll()  // Zezwól na dostęp do rejestracji
                                .anyRequest().authenticated()  // Pozostałe endpointy wymagają autoryzacji
                )
                .formLogin(withDefaults())  // Włącza domyślną stronę logowania
                .csrf(csrf -> csrf.disable());  // Wyłączenie CSRF

        return http.build();
    }
}
