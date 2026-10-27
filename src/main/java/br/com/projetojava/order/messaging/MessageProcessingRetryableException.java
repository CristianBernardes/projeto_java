package br.com.projetojava.order.messaging;

/**
 * Signals that a message processing failure was classified as transient by the messaging error
 * handling chain and should be retried by the listener container.
 *
 * <p>Thrown instead of letting the original exception propagate so that every retryable failure is
 * funneled through a single, predictable exception type — regardless of which underlying cause
 * (database blip, optimistic lock, etc.) triggered it. The listener container's retry interceptor
 * ({@code spring.rabbitmq.listener.simple.retry}) retries on any uncaught exception, so this type
 * carries no special container configuration; it exists purely to make the intent explicit at the
 * throw site and in logs.
 */
public class MessageProcessingRetryableException extends RuntimeException {

  /**
   * @param message description of the transient failure
   * @param cause original exception that triggered the retry decision
   */
  public MessageProcessingRetryableException(String message, Throwable cause) {
    super(message, cause);
  }
}
