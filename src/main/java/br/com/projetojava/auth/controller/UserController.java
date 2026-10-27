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
@Tag(name = "Users", description = "Authenticated user profile and avatar management")
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
      summary = "Get my profile",
      description =
          "Returns the authenticated user's profile, including the avatar file identifier and"
              + " download URL when an avatar has been set. Requires authentication.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required",
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
      summary = "Upload my avatar",
      description =
          "Uploads an image (JPEG or PNG, max 10MB) and sets it as the authenticated user's"
              + " avatar. Requires authentication. Avatar upload is optional and can be replaced"
              + " at any time; previous uploads are kept as history, only the current pointer"
              + " changes.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Avatar uploaded and profile updated successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file (type not allowed or size exceeded)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> uploadAvatar(
      @RequestParam("file") MultipartFile file, Authentication authentication) {
    Long userId = authenticatedUserResolver.resolveUserId(authentication);
    return ResponseEntity.ok(userProfileService.uploadAvatar(userId, file));
  }
}
