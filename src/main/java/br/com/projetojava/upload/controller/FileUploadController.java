package br.com.projetojava.upload.controller;

import br.com.projetojava.shared.security.AuthenticatedUserResolver;
import br.com.projetojava.upload.dto.FileUploadResponse;
import br.com.projetojava.upload.service.FileUploadServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
@Tag(name = "Upload de Arquivos", description = "Gerenciamento de envio e download de arquivos")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

  private final FileUploadServicePort fileUploadService;
  private final AuthenticatedUserResolver authenticatedUserResolver;

  /**
   * Uploads one or more files and stores their metadata.
   *
   * <p>The files are optionally associated with a product (e.g., product photos) via the {@code
   * productId} query parameter — multiple files may be sent in a single request, since a product
   * can have several photos. The uploaded-by user is always resolved from the authenticated
   * principal.
   *
   * @param files multipart files to upload
   * @param productId optional product these files belong to
   * @param authentication current authenticated user
   * @return {@code 200 OK} with the metadata of every uploaded file
   */
  @Operation(
      summary = "Enviar arquivos",
      description =
          "Envia um ou mais arquivos (JPEG, PNG ou PDF, máx. 10MB cada). Requer autenticação."
              + " Opcionalmente associa os arquivos a um produto via o parâmetro de consulta"
              + " productId — envie múltiplos arquivos de uma vez para enviar várias fotos do"
              + " mesmo produto.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Arquivos enviados com sucesso",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array =
                        @ArraySchema(schema = @Schema(implementation = FileUploadResponse.class)))),
        @ApiResponse(
            responseCode = "400",
            description = "Arquivo inválido (tipo não permitido ou tamanho excedido)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "401",
            description = "Autenticação necessária",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<FileUploadResponse>> upload(
      @RequestParam("files") List<MultipartFile> files,
      @RequestParam(value = "productId", required = false) Long productId,
      Authentication authentication) {
    Long userId = authenticatedUserResolver.resolveUserId(authentication);
    return ResponseEntity.ok(fileUploadService.storeAll(files, productId, userId));
  }

  /**
   * Downloads a previously uploaded file.
   *
   * @param id file upload record identifier
   * @return {@code 200 OK} with the file content as an attachment
   */
  @Operation(
      summary = "Download de arquivo",
      description = "Faz o download de um arquivo pelo seu ID. Requer autenticação.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
        @ApiResponse(responseCode = "200", description = "Arquivo retornado com sucesso"),
        @ApiResponse(
            responseCode = "404",
            description = "Arquivo não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "401",
            description = "Autenticação necessária",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @GetMapping("/{id}")
  public ResponseEntity<Resource> download(
      @Parameter(description = "Identificador do arquivo", example = "1", required = true)
          @PathVariable
          Long id) {
    Resource resource = fileUploadService.load(id);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }
}
