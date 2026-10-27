package br.com.projetojava.messaging.errorhandling;

/**
 * Metadata about the message being processed when a failure occurred.
 *
 * <p>Plays the same role for the messaging error chain that {@code HttpServletRequest} plays for
 * the HTTP exception chain: context the handlers need to log and decide, without coupling the chain
 * to a specific broker's API (so the same chain works for both RabbitMQ and Kafka listeners).
 *
 * @param source broker/listener that was processing the message (e.g., {@code
 *     "rabbitmq.order.created.queue"})
 * @param messageId unique identifier of the message, if available
 */
public record MessageErrorContext(String source, String messageId) {}
