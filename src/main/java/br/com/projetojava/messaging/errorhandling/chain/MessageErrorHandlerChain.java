package br.com.projetojava.messaging.errorhandling.chain;

import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import java.util.Optional;

/**
 * Contract for a link in the message error handling chain (Chain of Responsibility).
 *
 * <p>Mirrors {@code br.com.projetojava.shared.handler.chain.ExceptionHandlerChain}, applied to
 * asynchronous message consumers (RabbitMQ/Kafka) instead of synchronous HTTP requests. Each link
 * decides whether it can resolve the received exception. If so, it returns a populated {@link
 * MessageErrorResolution}. If not, it returns {@link Optional#empty()} so the next link can try.
 *
 * <p>The chain is assembled by {@code MessagingErrorHandler} and traversed in order until a link
 * produces a resolution or the fallback link ends with {@link
 * br.com.projetojava.messaging.errorhandling.MessageErrorAction#REJECT_TO_DEAD_LETTER}.
 */
public interface MessageErrorHandlerChain {

  /**
   * Attempts to resolve the given exception into an action for the consumer to take.
   *
   * @param ex exception raised while processing the message
   * @param context metadata about the message being processed
   * @return {@link Optional} with the resolution if this link handled it, or empty to pass to the
   *     next
   */
  Optional<MessageErrorResolution> handle(Exception ex, MessageErrorContext context);
}
