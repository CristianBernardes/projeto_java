package br.com.projetojava.upload.controller;

import br.com.projetojava.shared.security.AuthenticatedUserResolver;
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
 * <p>All endpoints require a valid JWT token.
 */
@Tag(name = "File Uploads", description = "File upload and download management")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

  private final FileUploadServicePort fileUploadService;
  private final AuthenticatedUserResolver authenticatedUserResolver;

  /**
   * Uploads a file and stores its metadata.
   *
   * <p>The file is optionally associated with a product (e.g., a product photo) via the {@code
   * productId} query parameter. The uploaded-by user is always resolved from the authenticated
   * principal.
   *
   * @param file multipart file to upload
   * @param productId optional product this file belongs to
   * @param authentication current authenticated user
   * @return {@code 200 OK} with file metadata
   */
  @Operation(
      summary = "Upload file",
      description =
          "Uploads a file (JPEG, PNG or PDF, max 10MB). Requires authentication. Optionally"
              + " associates the file with a product via the productId query parameter.",
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
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<FileUploadResponse> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "productId", required = false) Long productId,
      Authentication authentication) {
    Long userId = authenticatedUserResolver.resolveUserId(authentication);
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
}
