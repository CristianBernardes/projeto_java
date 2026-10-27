package br.com.projetojava.outbox.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity implementing the <strong>Transactional Outbox</strong> pattern.
 *
 * <p>A domain event is written to this table in the <em>same database transaction</em> as the
 * business change it represents (e.g., creating an {@code Order}). This avoids the classic
 * <em>dual-write problem</em>: saving to the database and publishing to a message broker are two
 * separate systems, so doing both directly is not atomic — the DB commit can succeed while the
 * broker publish fails, or vice-versa.
 *
 * <p>A separate poller ({@code OutboxEventPublisher}) periodically reads unpublished rows and
 * publishes them to RabbitMQ/Kafka, then marks them as {@link #published}. If the poller crashes
 * mid-publish, the row remains unpublished and will simply be retried — at-least-once delivery by
 * construction.
 */
@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "aggregate_type", nullable = false, length = 50)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false)
  private Long aggregateId;

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "published", nullable = false)
  private boolean published;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "published_at")
  private LocalDateTime publishedAt;

  /**
   * Creates a new unpublished outbox event.
   *
   * @param aggregateType name of the aggregate that produced the event (e.g., {@code "Order"})
   * @param aggregateId identifier of the aggregate instance
   * @param eventType event type/name (e.g., {@code "order.created"})
   * @param payload JSON-serialized event payload
   * @return new {@link OutboxEvent} instance, not yet published
   */
  public static OutboxEvent create(
      String aggregateType, Long aggregateId, String eventType, String payload) {
    OutboxEvent event = new OutboxEvent();
    event.aggregateType = aggregateType;
    event.aggregateId = aggregateId;
    event.eventType = eventType;
    event.payload = payload;
    event.published = false;
    event.createdAt = LocalDateTime.now();
    return event;
  }

  /** Marks this event as successfully published to the message broker(s). */
  public void markPublished() {
    this.published = true;
    this.publishedAt = LocalDateTime.now();
  }

  /**
   * Two outbox events are equal if they share the same persisted {@code id}.
   *
   * @param o object to compare
   * @return {@code true} if both have the same non-null id
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OutboxEvent other)) return false;
    return id != null && id.equals(other.id);
  }

  /**
   * Constant hash based on type.
   *
   * @return constant hash for all {@link OutboxEvent} instances
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
