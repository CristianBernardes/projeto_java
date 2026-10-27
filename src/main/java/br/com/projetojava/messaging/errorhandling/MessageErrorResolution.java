package br.com.projetojava.messaging.errorhandling;

/**
 * Resolution produced by a {@link
 * br.com.projetojava.messaging.errorhandling.chain.MessageErrorHandlerChain} link.
 *
 * <p>Carries the {@link MessageErrorAction} the consumer should take, plus a human-readable reason
 * for logging — the messaging equivalent of the {@code ErrorResponse} produced by the HTTP
 * exception chain.
 *
 * @param action what the consumer should do with the message
 * @param reason human-readable explanation, used for logging
 */
public record MessageErrorResolution(MessageErrorAction action, String reason) {

  /**
   * Creates a resolution instructing the consumer to retry the message.
   *
   * @param reason human-readable explanation
   * @return resolution with {@link MessageErrorAction#RETRY}
   */
  public static MessageErrorResolution retry(String reason) {
    return new MessageErrorResolution(MessageErrorAction.RETRY, reason);
  }

  /**
   * Creates a resolution instructing the consumer to reject the message straight to the dead-letter
   * destination.
   *
   * @param reason human-readable explanation
   * @return resolution with {@link MessageErrorAction#REJECT_TO_DEAD_LETTER}
   */
  public static MessageErrorResolution rejectToDeadLetter(String reason) {
    return new MessageErrorResolution(MessageErrorAction.REJECT_TO_DEAD_LETTER, reason);
  }

  /**
   * Creates a resolution instructing the consumer to acknowledge the message as processed.
   *
   * @param reason human-readable explanation
   * @return resolution with {@link MessageErrorAction#ACKNOWLEDGE}
   */
  public static MessageErrorResolution acknowledge(String reason) {
    return new MessageErrorResolution(MessageErrorAction.ACKNOWLEDGE, reason);
  }
}
