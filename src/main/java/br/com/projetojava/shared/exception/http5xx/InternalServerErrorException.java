package br.com.projetojava.shared.exception.http5xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 500 Internal Server Error} — unexpected server error.
 *
 * <p>Use when an internal failure occurs that does not fit any other more specific error code.
 * Never expose internal technical details in the message returned to the client.
 */
public class InternalServerErrorException extends ApiException {

  /**
   * @param message generic error description (without internal details)
   */
  public InternalServerErrorException(String message) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }

  /**
   * @param message generic error description
   * @param cause original exception (for internal logging)
   */
  public InternalServerErrorException(String message, Throwable cause) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
  }
}
