package br.com.projetojava.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Input DTO for user login.
 *
 * @param email user email address
 * @param password plain-text password
 */
@Schema(description = "Credenciais para autenticação")
public record LoginRequest(
    @Schema(
            description = "Endereço de e-mail do usuário",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        String email,
    @Schema(
            description = "Senha do usuário",
            example = "secret123",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A senha é obrigatória.")
        String password) {}
