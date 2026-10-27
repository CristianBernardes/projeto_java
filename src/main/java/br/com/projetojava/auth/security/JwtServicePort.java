package br.com.projetojava.auth.security;

import java.time.LocalDateTime;

/**
 * Contract for JWT token generation and validation.
 *
 * <p>Consumers ({@link JwtAuthFilter}, {@link br.com.projetojava.auth.service.AuthService}) depend
 * on this interface, never on the concrete implementation, respecting the <em>Dependency Inversion
 * Principle</em>.
 */
public interface JwtServicePort {

  /**
   * Generates a signed JWT token for the given subject and role.
   *
   * @param subject token subject (typically the user email)
   * @param role user role stored as a claim
   * @return signed JWT string
   */
  String generateToken(String subject, String role);

  /**
   * Extracts the subject (email) from a JWT token.
   *
   * @param token JWT string
   * @return subject claim value
   */
  String extractSubject(String token);

  /**
   * Extracts the role claim from a JWT token.
   *
   * @param token JWT string
   * @return role claim value
   */
  String extractRole(String token);

  /**
   * Extracts the expiration date/time from a JWT token.
   *
   * @param token JWT string
   * @return expiration as {@link LocalDateTime}
   */
  LocalDateTime extractExpiration(String token);

  /**
   * Validates whether the token is well-formed, signed correctly and not expired.
   *
   * @param token JWT string
   * @return {@code true} if the token is valid
   */
  boolean isTokenValid(String token);
}
