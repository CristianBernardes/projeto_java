package br.com.projetojava.shared.handler;

import br.com.projetojava.shared.handler.chain.ApiExceptionHandler;
import br.com.projetojava.shared.handler.chain.FallbackExceptionHandler;
import br.com.projetojava.shared.handler.chain.SpringWebExceptionHandler;
import br.com.projetojava.shared.handler.chain.ValidationExceptionHandler;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Single entry point for API exception handling.
 *
 * <p>Assembles the Chain of Responsibility once at initialization ({@link PostConstruct}), in
 * order:
 *
 * <ol>
 *   <li>{@link ApiExceptionHandler} — domain exceptions ({@code ApiException} and subclasses)
 *   <li>{@link ValidationExceptionHandler} — Bean Validation errors ({@code @Valid})
 *   <li>{@link SpringWebExceptionHandler} — Spring MVC protocol errors
 *   <li>{@link FallbackExceptionHandler} — any unhandled exception ({@code 500})
 * </ol>
 *
 * <p>No {@code try/catch} exists here: the chain encapsulates all decision logic.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ApiExceptionHandler apiExceptionHandler;
  private final ValidationExceptionHandler validationExceptionHandler;
  private final SpringWebExceptionHandler springWebExceptionHandler;
  private final FallbackExceptionHandler fallbackExceptionHandler;

  /**
   * Assembles the handler chain once after dependency injection.
   *
   * <p>Order matters: more specific links must come before more generic ones. {@link
   * FallbackExceptionHandler} is always last and never passes forward.
   */
  @PostConstruct
  void buildChain() {
    apiExceptionHandler
        .setNext(validationExceptionHandler)
        .setNext(springWebExceptionHandler)
        .setNext(fallbackExceptionHandler);
  }

  /**
   * Intercepts all exceptions not handled by controllers and routes them through the chain.
   *
   * @param ex captured exception
   * @param request original HTTP request
   * @return standardized error response with the appropriate HTTP status
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
    ErrorResponse response =
        apiExceptionHandler
            .handle(ex, request)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "FallbackExceptionHandler did not handle the exception — chain misconfigured."));

    return ResponseEntity.status(response.status()).body(response);
  }
}
