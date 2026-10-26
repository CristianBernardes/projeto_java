package br.com.projetojava.upload.controller;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.auth.repository.UserRepository;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import br.com.projetojava.shared.exception.http4xx.UnauthorizedException;
import br.com.projetojava.upload.dto.FileUploadResponse;
import br.com.projetojava.upload.service.FileUploadServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for file upload and download endpoints.
 *
 * <p>Files are always linked to a product ({@code productId}) and the authenticated user. All
 * endpoints require a valid JWT token.
 */
@Tag(name = "File Uploads", description = "File upload and download management")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

  private final FileUploadServicePort fileUploadService;
  private final UserRepository userRepository;

  /**
   * Uploads a file and associates it with a product.
   *
   * @param file multipart file to upload
   * @param productId ID of the product this file belongs to
   * @param authentication current authenticated user
   * @return {@code 200 OK} with file metadata
   */
  @Operation(
      summary = "Upload file",
      description =
          "Uploads a file (JPEG, PNG or PDF, max 10 MB) and links it to a product."
              + " Requires authentication.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "File uploaded successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FileUploadResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file (type not allowed or size exceeded)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "404",
            description = "Product or user not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<FileUploadResponse> upload(
      @RequestParam("file") MultipartFile file,
      @Parameter(description = "ID of the product this file belongs to", required = true)
          @RequestParam("productId")
          Long productId,
      Authentication authentication) {
    Long userId = resolveUserId(authentication);
    return ResponseEntity.ok(fileUploadService.store(file, productId, userId));
  }

  /**
   * Downloads a previously uploaded file.
   *
   * @param id file upload record identifier
   * @return {@code 200 OK} with the file content as an attachment
   */
  @Operation(
      summary = "Download file",
      description = "Downloads a file by its ID. Requires authentication.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(responseCode = "200", description = "File returned successfully"),
        @ApiResponse(
            responseCode = "404",
            description = "File not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @GetMapping("/{id}")
  public ResponseEntity<Resource> download(
      @Parameter(description = "File identifier", example = "1", required = true) @PathVariable
          Long id) {
    Resource resource = fileUploadService.load(id);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  /**
   * Resolves the authenticated user's database ID from the JWT principal (email).
   *
   * @param authentication Spring Security authentication object
   * @return user ID
   * @throws UnauthorizedException if not authenticated
   * @throws ResourceNotFoundException if the user record no longer exists
   */
  private Long resolveUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("Authentication required.");
    }
    String email = (String) authentication.getPrincipal();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));
    return user.getId();
  }
}
