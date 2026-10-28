package br.com.projetojava.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Input DTO for user registration.
 *
 * @param email user email address
 * @param password plain-text password (will be hashed before storage)
 */
@Schema(description = "Dados para registro de usuário")
public record RegisterRequest(
    @Schema(
            description = "Endereço de e-mail do usuário",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        String email,
    @Schema(
            description = "Senha (mínimo 8 caracteres)",
            example = "secret123",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        String password) {}
