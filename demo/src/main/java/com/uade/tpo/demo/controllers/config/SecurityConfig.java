package com.uade.tpo.demo.controllers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.uade.tpo.demo.entity.Role;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    // Modelo de e-commerce propio:
    //   USER  = comprador. Navega el catalogo y compra.
    //   ADMIN = el emprendimiento. Publica productos, maneja stock,
    //           crea categorias y administra las cuentas de usuario.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req

                        // --- Publico: registro y login ---
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/error/**").permitAll()

                        // --- Catalogo: leer lo puede cualquier usuario logueado ---
                        .requestMatchers(HttpMethod.GET, "/products/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/categories/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasAuthority(Role.ADMIN.name())

                        // --- Catalogo: escribir solo el ADMIN (es quien vende) ---
                        .requestMatchers(HttpMethod.POST, "/products/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, "/products/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/categories/**").hasAuthority(Role.ADMIN.name())

                        // --- Carrito y compra: solo el comprador ---
                        .requestMatchers("/cart/**").hasAuthority(Role.USER.name())

                        // --- Gestion de cuentas y permisos: solo el ADMIN ---
                        .requestMatchers("/users/**").hasAuthority(Role.ADMIN.name())

                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}