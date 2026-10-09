package com.uade.tpo.demo.controllers.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
    //   USER  = comprador. Navega el catalogo, compra y ve su historial.
    //   ADMIN = el emprendimiento. Publica productos, maneja stock,
    //           crea categorias y administra las cuentas de usuario.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Habilita CORS usando el bean corsConfigurationSource de mas abajo.
                // Sin esto, el navegador bloquea cualquier llamada desde el front.
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req

                        // El navegador manda un OPTIONS (preflight) antes de cada
                        // request con token. Nunca lleva credenciales, asi que
                        // tiene que pasar libre.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- Perfil del usuario autenticado ---
                        // Va ANTES del permitAll de /api/v1/auth/**, porque en
                        // Spring Security gana la primera regla que coincide.
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()

                        // --- Publico: registro y login ---
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/error/**").permitAll()

                        // --- Imagenes subidas ---
                        // Verlas es publico (van en el catalogo); subirlas,
                        // solo el administrador.
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers("/uploads/**").hasAuthority(Role.ADMIN.name())

                        // --- Catalogo: leer es publico ---
                        // Cualquiera puede ver los productos sin registrarse,
                        // como en cualquier tienda online. El login se pide
                        // recien al querer comprar.
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()

                        // --- Catalogo: cualquier otra operacion, solo el ADMIN ---
                        // Al no listar metodo por metodo, todo lo que no sea GET
                        // (POST, PUT, PATCH, DELETE, y cualquier endpoint que se
                        // agregue despues) queda protegido automaticamente.
                        .requestMatchers("/products/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers("/categories/**").hasAuthority(Role.ADMIN.name())

                        // --- Carrito y compra: solo el comprador ---
                        .requestMatchers("/cart/**").hasAuthority(Role.USER.name())

                        // --- Historial de compras: solo el comprador ---
                        .requestMatchers("/orders/**").hasAuthority(Role.USER.name())

                        // --- Gestion de cuentas y permisos: solo el ADMIN ---
                        .requestMatchers("/users/**").hasAuthority(Role.ADMIN.name())

                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Permite que el front, que corre en otro puerto, llame a esta API.
     *
     * El navegador considera "otro origen" a cualquier combinacion distinta
     * de protocolo + host + puerto. Como la API esta en localhost:4002 y el
     * front en localhost:5173, son origenes distintos y sin esta
     * configuracion el navegador bloquea la respuesta.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Puertos de desarrollo: 5173 es el de Vite (npm run dev),
        // 4173 el de vite preview y 3000 el de Create React App.
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:4173",
                "http://localhost:3000"));

        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // El front manda Authorization y Content-Type.
        config.setAllowedHeaders(Arrays.asList("*"));

        // Deja que el front lea la cabecera Location de un 201 Created.
        config.setExposedHeaders(Arrays.asList("Location"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
