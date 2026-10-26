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
@Schema(description = "Credentials for authentication")
public record LoginRequest(
    @Schema(
            description = "User email address",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        String email,
    @Schema(
            description = "User password",
            example = "secret123",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required.")
        String password) {}
