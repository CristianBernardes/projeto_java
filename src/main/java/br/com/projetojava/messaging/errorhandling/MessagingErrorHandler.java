package br.com.projetojava.messaging.errorhandling;

import br.com.projetojava.messaging.errorhandling.chain.BusinessErrorMessageHandler;
import br.com.projetojava.messaging.errorhandling.chain.DeserializationErrorMessageHandler;
import br.com.projetojava.messaging.errorhandling.chain.FallbackMessageErrorHandler;
import br.com.projetojava.messaging.errorhandling.chain.TransientErrorMessageHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Single entry point for message processing exception handling.
 *
 * <p>Mirrors {@code br.com.projetojava.shared.handler.GlobalExceptionHandler}, but for asynchronous
 * message consumers (RabbitMQ/Kafka) instead of HTTP requests. Assembles the Chain of
 * Responsibility once at initialization ({@link PostConstruct}), in order:
 *
 * <ol>
 *   <li>{@link BusinessErrorMessageHandler} — permanent business errors ({@code
 *       ResourceNotFoundException})
 *   <li>{@link DeserializationErrorMessageHandler} — malformed/unparseable payloads
 *   <li>{@link TransientErrorMessageHandler} — transient infrastructure errors
 *   <li>{@link FallbackMessageErrorHandler} — any unhandled exception (dead-letter)
 * </ol>
 *
 * <p>Implements {@link MessagingErrorHandlerPort} so consumers depend on the interface, never on
 * this concrete assembly.
 */
@Component
@RequiredArgsConstructor
public class MessagingErrorHandler implements MessagingErrorHandlerPort {

  private final BusinessErrorMessageHandler businessErrorMessageHandler;
  private final DeserializationErrorMessageHandler deserializationErrorMessageHandler;
  private final TransientErrorMessageHandler transientErrorMessageHandler;
  private final FallbackMessageErrorHandler fallbackMessageErrorHandler;

  /**
   * Assembles the handler chain once after dependency injection.
   *
   * <p>Order matters: more specific links must come before more generic ones. {@link
   * FallbackMessageErrorHandler} is always last and never passes forward.
   */
  @PostConstruct
  void buildChain() {
    businessErrorMessageHandler
        .setNext(deserializationErrorMessageHandler)
        .setNext(transientErrorMessageHandler)
        .setNext(fallbackMessageErrorHandler);
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalStateException if the chain is misconfigured and no link produced a resolution
   *     (should never happen, since {@link FallbackMessageErrorHandler} always handles)
   */
  @Override
  public MessageErrorResolution resolve(Exception ex, MessageErrorContext context) {
    return businessErrorMessageHandler
        .handle(ex, context)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "FallbackMessageErrorHandler did not handle the exception — chain"
                        + " misconfigured."));
  }
}
