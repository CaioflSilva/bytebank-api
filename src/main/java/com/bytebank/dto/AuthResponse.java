package com.bytebank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de autenticação contendo o token JWT")
public class AuthResponse {

    @Schema(description = "Token JWT para uso nas requisições", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Tipo do token", example = "Bearer")
    private String tipo = "Bearer";

    public AuthResponse(String token) {
        this.token = token;
    }
}
