package br.com.projetojava.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publishes order processing messages to the RabbitMQ {@code order.exchange}.
 *
 * <p>Used for work that must be executed exactly by <strong>one</strong> consumer (stock
 * reservation), as opposed to {@link KafkaOrderEventPublisher}, which broadcasts events for
 * independent streaming consumers.
 *
 * <p>Implements {@link OrderMessagePublisherPort} to respect the <em>Dependency Inversion
 * Principle</em>: consumers depend on the interface, not on this concrete class.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitOrderMessagePublisher implements OrderMessagePublisherPort {

  @Value("${app.messaging.rabbitmq.order-exchange}")
  private String orderExchange;

  @Value("${app.messaging.rabbitmq.order-created-routing-key}")
  private String orderCreatedRoutingKey;

  private final RabbitTemplate rabbitTemplate;

  /**
   * Publishes an {@code order.created} message to the RabbitMQ work queue.
   *
   * @param messageId unique identifier for this message (used by the consumer's idempotency guard
   *     to detect redeliveries)
   * @param payload event payload, serialized to JSON by the configured message converter
   */
  @Override
  public void publishOrderCreated(String messageId, Object payload) {
    log.info(
        "Publishing to RabbitMQ exchange='{}', routing_key='{}', message_id='{}'.",
        orderExchange,
        orderCreatedRoutingKey,
        messageId);
    rabbitTemplate.convertAndSend(
        orderExchange,
        orderCreatedRoutingKey,
        payload,
        message -> {
          message.getMessageProperties().setMessageId(messageId);
          return message;
        });
  }
}
