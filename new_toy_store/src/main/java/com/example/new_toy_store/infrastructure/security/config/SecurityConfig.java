package com.example.new_toy_store.infrastructure.security.config;

import com.example.new_toy_store.infrastructure.security.jwt.JwtAuthenticationEntryPoint;
import com.example.new_toy_store.infrastructure.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/users/register",
                                "/users/login",
                                "/users/verify",
                                "/users/forgot-password",
                                "/users/reset-password"
                        ).permitAll()
                        .requestMatchers("/payments/vnpay-return", "/payments/vnpay-ipn").permitAll()

                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/products/**").hasAnyRole("STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasAnyRole("STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/products/**").hasAnyRole("STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/categories/**").hasAnyRole("STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasAnyRole("STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/categories/**").hasAnyRole("STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers("/users/me", "/users/me/**").authenticated()
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        .requestMatchers("/cart/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/orders/**").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/orders/**").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/orders/**").hasAnyRole("STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/orders/**").hasAnyRole("STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/orders/**").hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/payments/**").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/payments/**").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/payments/**").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/payments/**").hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/shipments/**").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/shipments/**").hasAnyRole("STAFF", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/shipments/**").hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers("/suppliers/**").hasAnyRole("STAFF", "MANAGER", "ADMIN")
                        .requestMatchers("/statistics/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/notifications/broadcast").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/notifications/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
