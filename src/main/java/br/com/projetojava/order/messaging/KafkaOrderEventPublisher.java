package br.com.projetojava.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes order domain events to the Kafka {@code order-events} topic.
 *
 * <p>Unlike RabbitMQ (used as a work queue for stock reservation), this topic exists purely for
 * <strong>event streaming</strong>: auditing, analytics, and any future independent consumer that
 * wants to react to order lifecycle changes without competing with the stock reservation consumer.
 * The order id is used as the Kafka message key so all events for the same order land in the same
 * partition, preserving per-order ordering.
 *
 * <p>Implements {@link OrderEventPublisherPort} to respect the <em>Dependency Inversion
 * Principle</em>: consumers depend on the interface, not on this concrete class.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderEventPublisher implements OrderEventPublisherPort {

  @Value("${app.messaging.kafka.order-events-topic}")
  private String orderEventsTopic;

  private final KafkaTemplate<String, Object> kafkaTemplate;

  /**
   * Publishes an order event to the {@code order-events} topic, keyed by order id.
   *
   * @param orderId identifier of the order the event refers to (used as the partition key)
   * @param eventType event type/name (e.g., {@code "order.created"})
   * @param payload event payload, serialized to JSON by the configured Kafka producer
   */
  @Override
  public void publish(Long orderId, String eventType, Object payload) {
    log.info(
        "Publicando no tópico Kafka='{}', order_id={}, event_type='{}'.",
        orderEventsTopic,
        orderId,
        eventType);
    kafkaTemplate.send(orderEventsTopic, String.valueOf(orderId), payload);
  }
}
