package br.com.projetojava.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Output DTO representing the authenticated user's profile.
 *
 * @param id unique identifier
 * @param email user email
 * @param role user role
 * @param avatarId identifier of the {@code file_uploads} row used as avatar, or {@code null} if the
 *     user has no avatar set
 * @param avatarDownloadUrl relative URL to download the avatar file, or {@code null} if the user
 *     has no avatar set
 */
@Schema(description = "Perfil do usuário autenticado")
public record UserResponse(
    @Schema(description = "Identificador único", example = "1") Long id,
    @Schema(description = "E-mail do usuário", example = "user@example.com") String email,
    @Schema(description = "Papel do usuário", example = "USER") String role,
    @Schema(description = "Identificador do arquivo de avatar", example = "3", nullable = true)
        Long avatarId,
    @Schema(
            description = "URL relativa para download do avatar",
            example = "/api/v1/files/3",
            nullable = true)
        String avatarDownloadUrl) {}
