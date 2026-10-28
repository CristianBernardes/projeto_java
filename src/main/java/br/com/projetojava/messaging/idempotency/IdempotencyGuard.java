package br.com.projetojava.messaging.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * Guard against duplicate processing of at-least-once message deliveries.
 *
 * <p>Checks whether a message was already processed and, if not, atomically records it. The unique
 * constraint on {@code processed_messages.message_id} is the real safety net: even under concurrent
 * delivery of the same message (two consumer threads, or a retry racing the original delivery),
 * only one caller succeeds in inserting the marker — the race is resolved by the database, not by
 * application logic.
 *
 * <p>Implements {@link IdempotencyGuardPort} to respect the <em>Dependency Inversion
 * Principle</em>: consumers depend on the interface, not on this concrete class.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyGuard implements IdempotencyGuardPort {

  private final ProcessedMessageRepository processedMessageRepository;

  /**
   * Checks whether the given message ID has already been processed.
   *
   * @param messageId unique identifier of the incoming message
   * @return {@code true} if this message was already processed and should be skipped
   */
  @Override
  public boolean isAlreadyProcessed(String messageId) {
    return processedMessageRepository.existsById(messageId);
  }

  /**
   * Records the given message ID as processed.
   *
   * <p>If a concurrent caller already inserted the same ID, the unique constraint violation is
   * caught and logged — the message is still correctly marked as processed exactly once.
   *
   * @param messageId unique identifier of the message being marked as processed
   */
  @Override
  public void markProcessed(String messageId) {
    try {
      processedMessageRepository.save(ProcessedMessage.create(messageId));
    } catch (DataIntegrityViolationException e) {
      log.warn(
          "Mensagem id='{}' já havia sido marcada como processada por um consumidor concorrente.",
          messageId);
    }
  }
}
