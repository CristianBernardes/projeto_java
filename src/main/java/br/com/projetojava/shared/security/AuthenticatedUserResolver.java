package br.com.projetojava.shared.security;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.auth.repository.UserRepository;
import br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException;
import br.com.projetojava.shared.exception.http4xx.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Resolves the authenticated user's database identifier from a Spring Security {@link
 * Authentication}.
 *
 * <p>{@link br.com.projetojava.auth.security.JwtAuthFilter} sets the authentication principal to
 * the user's email (the JWT subject); this component looks up the corresponding {@link User} row so
 * controllers can work with the numeric id instead of parsing the principal themselves.
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUserResolver {

  private final UserRepository userRepository;

  /**
   * Resolves the database id of the currently authenticated user.
   *
   * @param authentication Spring Security authentication object
   * @return the authenticated user's id
   * @throws UnauthorizedException if there is no authenticated principal
   * @throws ResourceNotFoundException if the user record no longer exists
   */
  public Long resolveUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    String email = (String) authentication.getPrincipal();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new ResourceNotFoundException("Usuário autenticado não encontrado."));
    return user.getId();
  }
}
