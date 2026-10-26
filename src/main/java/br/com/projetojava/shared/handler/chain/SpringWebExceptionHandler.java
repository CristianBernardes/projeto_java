package br.com.projetojava.shared.handler.chain;

import br.com.projetojava.shared.handler.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Link 3 — handles exceptions thrown by the Spring MVC infrastructure.
 *
 * <p>Covers HTTP protocol errors that the framework throws before reaching the controller:
 *
 * <ul>
 *   <li>{@code 400} — unreadable body, missing parameter, type mismatch
 *   <li>{@code 404} — no handler found for the path
 *   <li>{@code 405} — HTTP method not supported by the endpoint
 *   <li>{@code 415} — unsupported Content-Type
 * </ul>
 */
@Component
public class SpringWebExceptionHandler extends AbstractExceptionHandler {

  /**
   * {@inheritDoc}
   *
   * @return {@code true} for the Spring MVC exceptions mapped by this link
   */
  @Override
  protected boolean supports(Exception ex) {
    return ex instanceof HttpMessageNotReadableException
        || ex instanceof MissingServletRequestParameterException
        || ex instanceof MethodArgumentTypeMismatchException
        || ex instanceof NoHandlerFoundException
        || ex instanceof HttpRequestMethodNotSupportedException
        || ex instanceof HttpMediaTypeNotSupportedException;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Maps each Spring exception type to the correct semantic HTTP status.
   */
  @Override
  protected ErrorResponse process(Exception ex, HttpServletRequest request) {
    HttpStatus status;
    String message;

    if (ex instanceof HttpMessageNotReadableException) {
      status = HttpStatus.BAD_REQUEST;
      message = "Request body is missing or malformed.";

    } else if (ex instanceof MissingServletRequestParameterException missing) {
      status = HttpStatus.BAD_REQUEST;
      message = "Required parameter missing: '%s'.".formatted(missing.getParameterName());

    } else if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
      status = HttpStatus.BAD_REQUEST;
      message =
          "Invalid value for parameter '%s': '%s'."
              .formatted(mismatch.getName(), mismatch.getValue());

    } else if (ex instanceof NoHandlerFoundException) {
      status = HttpStatus.NOT_FOUND;
      message =
          "Resource not found: %s %s.".formatted(request.getMethod(), request.getRequestURI());

    } else if (ex instanceof HttpRequestMethodNotSupportedException notSupported) {
      status = HttpStatus.METHOD_NOT_ALLOWED;
      message = "Method '%s' not allowed for this endpoint.".formatted(notSupported.getMethod());

    } else {
      status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
      message = "Content-Type not supported.";
    }

    return ErrorResponse.of(status, message, request.getRequestURI());
  }
}
