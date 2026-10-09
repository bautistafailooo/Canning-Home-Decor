package com.uade.tpo.demo.controllers.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uade.tpo.demo.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    @JsonProperty("access_token")
    private String accessToken;

    // Datos del usuario que acaba de entrar. Con esto el front sabe el
    // nombre y el rol en la misma respuesta del login, sin tener que hacer
    // un GET /me justo despues de un POST.
    private Long id;
    private String email;
    private String username;
    private String name;
    private String surname;
    private Role role;
}
