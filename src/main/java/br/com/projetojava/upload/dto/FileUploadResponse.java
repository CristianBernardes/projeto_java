package br.com.projetojava.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Output DTO representing an uploaded file in the API response.
 *
 * @param id unique identifier
 * @param originalName original file name
 * @param contentType MIME type
 * @param sizeBytes file size in bytes
 * @param productId ID of the product this file belongs to
 * @param userId ID of the user who uploaded the file
 * @param createdAt upload date/time
 */
@Schema(description = "Uploaded file metadata")
public record FileUploadResponse(
    @Schema(description = "Unique identifier", example = "1") Long id,
    @Schema(description = "Original file name", example = "document.pdf") String originalName,
    @Schema(description = "MIME type", example = "application/pdf") String contentType,
    @Schema(description = "File size in bytes", example = "204800") long sizeBytes,
    @Schema(description = "ID of the product this file belongs to", example = "5") Long productId,
    @Schema(description = "ID of the user who uploaded the file", example = "2") Long userId,
    @Schema(description = "Upload date and time") LocalDateTime createdAt) {}
