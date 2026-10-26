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
@Schema(description = "Data for user registration")
public record RegisterRequest(
    @Schema(
            description = "User email address",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        String email,
    @Schema(
            description = "Password (min 8 characters)",
            example = "secret123",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required.")
        @Size(min = 8, message = "Password must have at least 8 characters.")
        String password) {}
