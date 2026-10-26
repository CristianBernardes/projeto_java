package br.com.projetojava.shared.exception.http4xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 404 Not Found} — the requested resource does not exist or was not found.
 *
 * <p>Use when an entity searched by identifier does not exist in the system.
 */
public class ResourceNotFoundException extends ApiException {

  /**
   * @param message description of the resource not found
   */
  public ResourceNotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }

  /**
   * @param message description of the resource not found
   * @param cause original exception
   */
  public ResourceNotFoundException(String message, Throwable cause) {
    super(HttpStatus.NOT_FOUND, message, cause);
  }
}
