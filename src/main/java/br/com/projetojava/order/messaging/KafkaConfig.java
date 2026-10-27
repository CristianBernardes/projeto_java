package br.com.projetojava.order.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topology for order domain events.
 *
 * <p>Declares the {@code order-events} topic as a Spring bean so {@code KafkaAdmin} creates it
 * automatically on application startup — the topic always exists with the expected
 * partition/replication settings, regardless of whether the broker's {@code
 * auto.create.topics.enable} is on. Mirrors the role {@link RabbitMqConfig} plays for RabbitMQ.
 */
@Configuration
public class KafkaConfig {

  @Value("${app.messaging.kafka.order-events-topic}")
  private String orderEventsTopicName;

  @Value("${app.messaging.kafka.order-events-topic-partitions}")
  private int orderEventsTopicPartitions;

  @Value("${app.messaging.kafka.order-events-topic-replicas}")
  private short orderEventsTopicReplicas;

  /**
   * Event stream topic for order lifecycle events (auditing, independent consumers).
   *
   * @return the {@code order-events} topic, created on startup if it does not already exist
   */
  @Bean
  public NewTopic orderEventsTopic() {
    return TopicBuilder.name(orderEventsTopicName)
        .partitions(orderEventsTopicPartitions)
        .replicas(orderEventsTopicReplicas)
        .build();
  }
}
