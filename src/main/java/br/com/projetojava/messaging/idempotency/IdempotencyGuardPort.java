package br.com.projetojava.messaging.idempotency;

/**
 * Contract for guarding against duplicate processing of at-least-once message deliveries.
 *
 * <p>Consumers ({@link br.com.projetojava.order.messaging.StockReservationConsumer}) depend on this
 * interface, never on the concrete implementation, respecting the <em>Dependency Inversion
 * Principle</em>.
 */
public interface IdempotencyGuardPort {

  /**
   * Checks whether the given message ID has already been processed.
   *
   * @param messageId unique identifier of the incoming message
   * @return {@code true} if this message was already processed and should be skipped
   */
  boolean isAlreadyProcessed(String messageId);

  /**
   * Records the given message ID as processed.
   *
   * @param messageId unique identifier of the message being marked as processed
   */
  void markProcessed(String messageId);
}
