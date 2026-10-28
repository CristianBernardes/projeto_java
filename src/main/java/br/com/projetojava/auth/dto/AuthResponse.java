package br.com.projetojava.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Output DTO returned after successful authentication.
 *
 * @param token JWT bearer token
 * @param email authenticated user email
 * @param role user role
 * @param expiresAt token expiration date/time
 */
@Schema(description = "Resposta de autenticação contendo o token JWT")
public record AuthResponse(
    @Schema(description = "Token JWT (bearer)") String token,
    @Schema(description = "E-mail do usuário autenticado", example = "user@example.com")
        String email,
    @Schema(description = "Papel do usuário", example = "USER") String role,
    @Schema(description = "Data e hora de expiração do token") LocalDateTime expiresAt) {}
