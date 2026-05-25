package com.sitmmio.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // desactiva CSRF
            .csrf(csrf -> csrf.disable())

            // permite H2 console
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // permisos
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/h2-console/**",
                    "/css/**",
                    "/js/**"
                ).permitAll()

                .anyRequest().authenticated()
            )

            // login por defecto
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}