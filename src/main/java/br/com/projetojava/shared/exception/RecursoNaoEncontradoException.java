package br.com.projetojava.shared.exception;

/**
 * Exceção lançada quando um recurso solicitado não é encontrado no sistema.
 *
 * <p>Mapeada para {@code 404 Not Found} pelo {@link
 * br.com.projetojava.shared.handler.GlobalExceptionHandler}.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

  /**
   * @param mensagem descrição do recurso não encontrado
   */
  public RecursoNaoEncontradoException(String mensagem) {
    super(mensagem);
  }
}
