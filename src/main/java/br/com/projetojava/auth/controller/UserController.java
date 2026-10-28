package br.com.projetojava.auth.controller;

import br.com.projetojava.auth.dto.UserResponse;
import br.com.projetojava.auth.service.UserProfileServicePort;
import br.com.projetojava.shared.security.AuthenticatedUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for the authenticated user's own profile and avatar.
 *
 * <p>All endpoints require a valid JWT token and always act on the user resolved from the
 * authenticated principal — there is no way to read or change another user's profile through this
 * controller.
 */
@Tag(name = "Usuários", description = "Gerenciamento de perfil e avatar do usuário autenticado")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserProfileServicePort userProfileService;
  private final AuthenticatedUserResolver authenticatedUserResolver;

  /**
   * Returns the authenticated user's profile, including avatar info if set.
   *
   * @param authentication current authenticated user
   * @return {@code 200 OK} with the user profile
   */
  @Operation(
      summary = "Obter meu perfil",
      description =
          "Retorna o perfil do usuário autenticado, incluindo o identificador do arquivo de avatar"
              + " e a URL de download quando um avatar foi definido. Requer autenticação.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Perfil retornado com sucesso",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Autenticação necessária",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @GetMapping("/me")
  public ResponseEntity<UserResponse> me(Authentication authentication) {
    Long userId = authenticatedUserResolver.resolveUserId(authentication);
    return ResponseEntity.ok(userProfileService.getProfile(userId));
  }

  /**
   * Uploads a new avatar for the authenticated user and sets it as their current avatar.
   *
   * <p>The avatar upload is not mandatory for a user — this endpoint is only called when the user
   * chooses to set or replace their profile photo. Exactly one avatar file is kept pointed to at a
   * time; previous uploads remain in {@code file_uploads} as history.
   *
   * @param file multipart image file (JPEG or PNG, max size per the shared upload validation rules)
   * @param authentication current authenticated user
   * @return {@code 200 OK} with the updated user profile
   */
  @Operation(
      summary = "Enviar meu avatar",
      description =
          "Envia uma imagem (JPEG ou PNG, máx. 10MB) e a define como avatar do usuário"
              + " autenticado. Requer autenticação. O envio de avatar é opcional e pode ser"
              + " substituído em qualquer momento; envios anteriores são mantidos como histórico,"
              + " apenas o ponteiro atual muda.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Avatar enviado e perfil atualizado com sucesso",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Arquivo inválido (tipo não permitido ou tamanho excedido)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "401",
            description = "Autenticação necessária",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> uploadAvatar(
      @RequestParam("file") MultipartFile file, Authentication authentication) {
    Long userId = authenticatedUserResolver.resolveUserId(authentication);
    return ResponseEntity.ok(userProfileService.uploadAvatar(userId, file));
  }
}
