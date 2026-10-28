package br.com.projetojava.shared.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Standardized error response DTO for all API errors.
 *
 * <p>All chain handlers produce this object, ensuring the error contract is always the same
 * regardless of the exception type.
 *
 * <p>The {@code errors} field is omitted from JSON when empty ({@link
 * JsonInclude.Include#NON_EMPTY}).
 *
 * @param timestamp date and time when the error occurred
 * @param status numeric HTTP status code
 * @param error HTTP status name (e.g., {@code "Not Found"})
 * @param message human-readable error description
 * @param path request path that generated the error
 * @param errors list of detailed field errors (optional, present in {@code 422})
 */
@Schema(description = "Resposta padronizada de erro da API")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
    @Schema(description = "Data e hora do erro") LocalDateTime timestamp,
    @Schema(description = "Código de status HTTP", example = "404") int status,
    @Schema(description = "Nome do status HTTP", example = "Not Found") String error,
    @Schema(description = "Descrição do erro", example = "Produto não encontrado. id=42")
        String message,
    @Schema(description = "Caminho da requisição", example = "/api/v1/products/42") String path,
    @Schema(description = "Erros de campo (presente apenas em 422)") List<String> errors) {

  /**
   * Creates a simple error response without a field error list.
   *
   * @param status HTTP status
   * @param message error description
   * @param path request path
   * @return {@link ErrorResponse} instance
   */
  public static ErrorResponse of(HttpStatus status, String message, String path) {
    return new ErrorResponse(
        LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path, List.of());
  }

  /**
   * Creates an error response with a list of detailed field errors.
   *
   * @param status HTTP status
   * @param message main error description
   * @param errors list of field errors
   * @param path request path
   * @return {@link ErrorResponse} instance
   */
  public static ErrorResponse withErrors(
      HttpStatus status, String message, List<String> errors, String path) {
    return new ErrorResponse(
        LocalDateTime.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        path,
        errors != null ? List.copyOf(errors) : List.of());
  }
}
