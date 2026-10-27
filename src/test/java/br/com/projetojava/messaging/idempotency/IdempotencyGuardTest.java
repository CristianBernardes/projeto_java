package br.com.projetojava.messaging.idempotency;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyGuard")
class IdempotencyGuardTest {

  @Mock private ProcessedMessageRepository processedMessageRepository;

  @InjectMocks private IdempotencyGuard idempotencyGuard;

  @Test
  @DisplayName("isAlreadyProcessed — should return true when the message id exists")
  void isAlreadyProcessed_shouldReturnTrueWhenExists() {
    when(processedMessageRepository.existsById("msg-1")).thenReturn(true);

    assertThat(idempotencyGuard.isAlreadyProcessed("msg-1")).isTrue();
  }

  @Test
  @DisplayName("isAlreadyProcessed — should return false when the message id does not exist")
  void isAlreadyProcessed_shouldReturnFalseWhenAbsent() {
    when(processedMessageRepository.existsById("msg-2")).thenReturn(false);

    assertThat(idempotencyGuard.isAlreadyProcessed("msg-2")).isFalse();
  }

  @Test
  @DisplayName("markProcessed — should save a new processed message record")
  void markProcessed_shouldSaveRecord() {
    idempotencyGuard.markProcessed("msg-1");

    verify(processedMessageRepository).save(any(ProcessedMessage.class));
  }

  @Test
  @DisplayName(
      "markProcessed — should swallow DataIntegrityViolationException from a concurrent insert")
  void markProcessed_shouldSwallowConcurrentInsertViolation() {
    when(processedMessageRepository.save(any(ProcessedMessage.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate key"));

    assertThatCode(() -> idempotencyGuard.markProcessed("msg-1")).doesNotThrowAnyException();
  }
}
