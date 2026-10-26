package br.com.projetojava.shared.handler;

import br.com.projetojava.shared.exception.RecursoNaoEncontradoException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler global de exceções da API.
 *
 * <p>Centraliza o tratamento de erros, garantindo respostas consistentes em formato JSON para todos
 * os endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Trata recursos não encontrados ({@code 404}).
   *
   * @param ex exceção lançada
   * @return corpo de erro padronizado
   */
  @ExceptionHandler(RecursoNaoEncontradoException.class)
  public ResponseEntity<Map<String, Object>> handleNaoEncontrado(
      RecursoNaoEncontradoException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
  }

  /**
   * Trata erros de validação de Bean Validation ({@code 422}).
   *
   * @param ex exceção lançada pelo Spring ao falhar {@code @Valid}
   * @return corpo de erro com a lista de campos inválidos
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
    List<String> erros =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
    return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Dados inválidos.", erros);
  }

  /**
   * Trata argumentos ilegais lançados pelo domínio ({@code 400}).
   *
   * @param ex exceção lançada
   * @return corpo de erro padronizado
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleArgumentoIlegal(IllegalArgumentException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
  }

  // ── Helper ──────────────────────────────────────────────────────────────────

  private ResponseEntity<Map<String, Object>> buildResponse(
      HttpStatus status, String mensagem, Object detalhes) {
    Map<String, Object> corpo = new LinkedHashMap<>();
    corpo.put("timestamp", LocalDateTime.now());
    corpo.put("status", status.value());
    corpo.put("erro", status.getReasonPhrase());
    corpo.put("mensagem", mensagem);
    if (detalhes != null) {
      corpo.put("detalhes", detalhes);
    }
    return ResponseEntity.status(status).body(corpo);
  }
}
