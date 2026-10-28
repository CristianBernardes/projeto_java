package br.com.projetojava.messaging.errorhandling.chain;

import br.com.projetojava.messaging.errorhandling.MessageErrorContext;
import br.com.projetojava.messaging.errorhandling.MessageErrorResolution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Link 3 — handles transient infrastructure errors that are likely to succeed on retry.
 *
 * <p>Database connectivity blips ({@link DataAccessException}) and optimistic locking conflicts
 * ({@link OptimisticLockingFailureException}, e.g., two consumers racing on the same row) are
 * exactly the kind of failure the listener container's retry-with-backoff policy ({@code
 * spring.rabbitmq.listener.simple.retry}) exists for: the same message will very likely succeed a
 * moment later, once the connection recovers or the competing transaction finishes.
 */
@Slf4j
@Component
public class TransientErrorMessageHandler extends AbstractMessageErrorHandler {

  /**
   * {@inheritDoc}
   *
   * @return {@code true} for {@link DataAccessException} and {@link
   *     OptimisticLockingFailureException}
   */
  @Override
  protected boolean supports(Exception ex) {
    return ex instanceof DataAccessException || ex instanceof OptimisticLockingFailureException;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Always resolves to {@link
   * br.com.projetojava.messaging.errorhandling.MessageErrorAction#RETRY}.
   */
  @Override
  protected MessageErrorResolution process(Exception ex, MessageErrorContext context) {
    log.warn(
        "Erro transitório de infraestrutura para message_id='{}' de source='{}': {}. Haverá retry.",
        context.messageId(),
        context.source(),
        ex.getMessage());
    return MessageErrorResolution.retry(ex.getMessage());
  }
}
