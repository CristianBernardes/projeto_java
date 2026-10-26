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
@Schema(description = "Authentication response containing the JWT token")
public record AuthResponse(
    @Schema(description = "JWT bearer token") String token,
    @Schema(description = "Authenticated user email", example = "user@example.com") String email,
    @Schema(description = "User role", example = "USER") String role,
    @Schema(description = "Token expiration date and time") LocalDateTime expiresAt) {}
