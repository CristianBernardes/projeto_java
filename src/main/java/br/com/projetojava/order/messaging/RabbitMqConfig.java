package br.com.projetojava.order.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for asynchronous order processing.
 *
 * <p>Models a classic <strong>work queue</strong>: the {@code order.exchange} (topic) routes {@code
 * order.created} messages to {@code order.created.queue}, where one or more competing consumers
 * reserve stock. Failed/rejected/poison messages are routed to a <strong>Dead Letter
 * Exchange</strong> (DLX) and land in {@code order.created.dlq} instead of looping forever.
 */
@Configuration
public class RabbitMqConfig {

  @Value("${app.messaging.rabbitmq.order-exchange}")
  private String orderExchangeName;

  @Value("${app.messaging.rabbitmq.order-created-queue}")
  private String orderCreatedQueueName;

  @Value("${app.messaging.rabbitmq.order-created-routing-key}")
  private String orderCreatedRoutingKey;

  @Value("${app.messaging.rabbitmq.order-dlx}")
  private String orderDlxName;

  @Value("${app.messaging.rabbitmq.order-dlq}")
  private String orderDlqName;

  /**
   * Main topic exchange for order-related messages.
   *
   * @return the {@code order.exchange} topic exchange
   */
  @Bean
  public TopicExchange orderExchange() {
    return new TopicExchange(orderExchangeName);
  }

  /**
   * Dead Letter Exchange that receives messages rejected or expired from the main queue.
   *
   * @return the {@code order.dlx} direct-routed exchange
   */
  @Bean
  public TopicExchange orderDeadLetterExchange() {
    return new TopicExchange(orderDlxName);
  }

  /**
   * Main work queue for stock reservation, configured to dead-letter failed messages instead of
   * requeueing them forever (avoiding an infinite poison-message loop).
   *
   * @return the {@code order.created.queue} durable queue
   */
  @Bean
  public Queue orderCreatedQueue() {
    return QueueBuilder.durable(orderCreatedQueueName)
        .withArgument("x-dead-letter-exchange", orderDlxName)
        .withArgument("x-dead-letter-routing-key", orderCreatedRoutingKey)
        .build();
  }

  /**
   * Dead letter queue holding messages that could not be processed after retries.
   *
   * @return the {@code order.created.dlq} durable queue
   */
  @Bean
  public Queue orderDeadLetterQueue() {
    return QueueBuilder.durable(orderDlqName).build();
  }

  /**
   * Binds the main queue to the main exchange using the {@code order.created} routing key.
   *
   * @param orderCreatedQueue the main work queue
   * @param orderExchange the main topic exchange
   * @return the binding between queue and exchange
   */
  @Bean
  public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange orderExchange) {
    return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with(orderCreatedRoutingKey);
  }

  /**
   * Binds the dead letter queue to the dead letter exchange.
   *
   * @param orderDeadLetterQueue the dead letter queue
   * @param orderDeadLetterExchange the dead letter exchange
   * @return the binding between the DLQ and the DLX
   */
  @Bean
  public Binding orderDeadLetterBinding(
      Queue orderDeadLetterQueue, TopicExchange orderDeadLetterExchange) {
    return BindingBuilder.bind(orderDeadLetterQueue)
        .to(orderDeadLetterExchange)
        .with(orderCreatedRoutingKey);
  }

  /**
   * JSON message converter so producers/consumers exchange typed Java objects instead of raw bytes.
   *
   * @return a {@link Jackson2JsonMessageConverter}
   */
  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
