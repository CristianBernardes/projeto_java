package br.com.projetojava.shared.exception.http4xx;

import br.com.projetojava.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * {@code 429 Too Many Requests} — the client has exceeded the allowed request rate limit.
 *
 * <p>Use together with throttling or rate limiting mechanisms to protect the API from abuse.
 */
public class TooManyRequestsException extends ApiException {

  /**
   * @param message description of the exceeded limit
   */
  public TooManyRequestsException(String message) {
    super(HttpStatus.TOO_MANY_REQUESTS, message);
  }

  /**
   * @param message description of the exceeded limit
   * @param cause original exception
   */
  public TooManyRequestsException(String message, Throwable cause) {
    super(HttpStatus.TOO_MANY_REQUESTS, message, cause);
  }
}
