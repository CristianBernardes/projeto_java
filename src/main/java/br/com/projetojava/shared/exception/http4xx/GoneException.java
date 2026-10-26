package br.com.projetojava.shared.exception.http4xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 410 Gone} — the resource existed but has been permanently removed.
 *
 * <p>Unlike {@code 404}, {@code 410} indicates the removal is definitive and the client should not
 * retry. Use for permanently deleted resources (hard delete).
 */
public class GoneException extends ApiException {

  /**
   * @param message description of the removed resource
   */
  public GoneException(String message) {
    super(HttpStatus.GONE, message);
  }

  /**
   * @param message description of the removed resource
   * @param cause original exception
   */
  public GoneException(String message, Throwable cause) {
    super(HttpStatus.GONE, message, cause);
  }
}
