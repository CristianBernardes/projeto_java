package br.com.projetojava.messaging.errorhandling;

/**
 * Action a {@link br.com.projetojava.messaging.errorhandling.chain.MessageErrorHandlerChain} link
 * decides to take for a failed message.
 *
 * <p>Mirrors, for asynchronous message consumers, the role that an HTTP status code plays for the
 * synchronous exception chain ({@code br.com.projetojava.shared.handler.chain}): it is the single
 * piece of information the consumer needs to know how to react.
 */
public enum MessageErrorAction {

  /**
   * The error is transient (e.g., a database connection blip) and the message should be retried
   * with backoff by the listener container. Letting the exception propagate triggers this in Spring
   * AMQP/Kafka.
   */
  RETRY,

  /**
   * The error is permanent for this message (e.g., malformed payload, a business rule that will
   * never succeed on retry) and the message should be routed straight to the dead-letter
   * destination without wasting retry attempts.
   */
  REJECT_TO_DEAD_LETTER,

  /**
   * The condition is not actually an error from the message's point of view (e.g., the saga already
   * compensated the order) and the message should be acknowledged as successfully processed.
   */
  ACKNOWLEDGE
}
