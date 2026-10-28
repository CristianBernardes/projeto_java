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
 * @param createdAt upload date/time
 */
@Schema(description = "Metadados de arquivo enviado")
public record FileUploadResponse(
    @Schema(description = "Identificador único", example = "1") Long id,
    @Schema(description = "Nome original do arquivo", example = "document.pdf") String originalName,
    @Schema(description = "Tipo MIME", example = "application/pdf") String contentType,
    @Schema(description = "Tamanho do arquivo em bytes", example = "204800") long sizeBytes,
    @Schema(description = "Data e hora do envio") LocalDateTime createdAt) {}
