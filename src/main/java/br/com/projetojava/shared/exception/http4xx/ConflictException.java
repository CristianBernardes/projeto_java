package br.com.projetojava.shared.exception.http4xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 409 Conflict} — the request conflicts with the current state of the resource.
 *
 * <p>Use in situations such as: attempting to create a resource with an already existing
 * identifier, uniqueness violation, or version conflict in optimistic update operations.
 */
public class ConflictException extends ApiException {

  /**
   * @param message conflict description
   */
  public ConflictException(String message) {
    super(HttpStatus.CONFLICT, message);
  }

  /**
   * @param message conflict description
   * @param cause original exception
   */
  public ConflictException(String message, Throwable cause) {
    super(HttpStatus.CONFLICT, message, cause);
  }
}
