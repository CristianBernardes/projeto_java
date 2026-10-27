package br.com.projetojava.messaging.errorhandling;

/**
 * Contract for resolving a message processing exception into an actionable {@link
 * MessageErrorResolution}.
 *
 * <p>This is the single entry point consumers ({@code
 * br.com.projetojava.order.messaging.StockReservationConsumer}) call — analogous to how controllers
 * never build {@code ErrorResponse} themselves and instead rely on {@code GlobalExceptionHandler}.
 * Consumers depend on this interface, never on the concrete chain assembly, respecting the
 * <em>Dependency Inversion Principle</em>.
 */
public interface MessagingErrorHandlerPort {

  /**
   * Resolves the given exception into an action for the consumer to take.
   *
   * @param ex exception raised while processing the message
   * @param context metadata about the message being processed
   * @return resolution produced by the error handling chain
   */
  MessageErrorResolution resolve(Exception ex, MessageErrorContext context);
}
