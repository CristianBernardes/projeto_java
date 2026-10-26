package br.com.projetojava.shared.exception.http4xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 403 Forbidden} — the client is authenticated but does not have permission for the
 * resource.
 *
 * <p>Unlike {@code 401}, here the client's identity is known and the denial is intentional.
 */
public class ForbiddenException extends ApiException {

  /**
   * @param message error description
   */
  public ForbiddenException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }

  /**
   * @param message error description
   * @param cause original exception
   */
  public ForbiddenException(String message, Throwable cause) {
    super(HttpStatus.FORBIDDEN, message, cause);
  }
}
