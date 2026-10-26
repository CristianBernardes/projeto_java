package br.com.projetojava.shared.exception.http4xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 400 Bad Request} — the request contains invalid syntax or malformed parameters.
 *
 * <p>Use when the input cannot be processed due to a client error that does not fit field
 * validation ({@code 422}).
 */
public class BadRequestException extends ApiException {

  /**
   * @param message error description
   */
  public BadRequestException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }

  /**
   * @param message error description
   * @param cause original exception
   */
  public BadRequestException(String message, Throwable cause) {
    super(HttpStatus.BAD_REQUEST, message, cause);
  }
}
