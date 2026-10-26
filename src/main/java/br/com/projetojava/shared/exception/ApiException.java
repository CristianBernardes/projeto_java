package br.com.projetojava.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for all business and HTTP exceptions in the API.
 *
 * <p>Every exception intentionally thrown by the application must extend this class, providing the
 * corresponding {@link HttpStatus}. This allows the {@link
 * br.com.projetojava.shared.handler.chain.ApiExceptionHandler chain} to handle any subclass
 * uniformly, without needing a separate {@code @ExceptionHandler} per type.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * throw new ResourceNotFoundException("Product not found. id=42");
 * }</pre>
 */
public abstract class ApiException extends RuntimeException {

  private final HttpStatus status;

  /**
   * @param status HTTP status code this exception represents
   * @param message descriptive error message
   */
  protected ApiException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  /**
   * @param status HTTP status code this exception represents
   * @param message descriptive error message
   * @param cause original exception that caused this error
   */
  protected ApiException(HttpStatus status, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
  }

  /**
   * Returns the {@link HttpStatus} associated with this exception.
   *
   * @return HTTP status
   */
  public HttpStatus getStatus() {
    return status;
  }
}
