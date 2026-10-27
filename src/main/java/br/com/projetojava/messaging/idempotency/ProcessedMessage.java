package br.com.projetojava.messaging.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Marker record used by {@link IdempotencyGuard} to implement an idempotent consumer.
 *
 * <p>RabbitMQ and Kafka both give <strong>at-least-once</strong> delivery: a message can be
 * redelivered after a consumer crash, a network blip, or a broker-side retry. Without this guard, a
 * redelivered {@code order.created} message would debit stock twice for the same order.
 */
@Entity
@Table(name = "processed_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedMessage {

  @Id
  @Column(name = "message_id", length = 100)
  private String messageId;

  @Column(name = "processed_at", nullable = false)
  private LocalDateTime processedAt;

  /**
   * Creates a new processed-message record.
   *
   * @param messageId unique identifier of the message that was processed
   * @return new {@link ProcessedMessage} instance
   */
  public static ProcessedMessage create(String messageId) {
    ProcessedMessage processed = new ProcessedMessage();
    processed.messageId = messageId;
    processed.processedAt = LocalDateTime.now();
    return processed;
  }
}
