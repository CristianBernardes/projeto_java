package br.com.projetojava.auth.service;

import br.com.projetojava.auth.dto.AuthResponse;
import br.com.projetojava.auth.dto.LoginRequest;
import br.com.projetojava.auth.dto.RegisterRequest;

/**
 * Contract for authentication use cases.
 *
 * <p>Consumers depend on this interface, never on the concrete implementation, respecting the
 * <em>Dependency Inversion Principle</em>.
 */
public interface AuthServicePort {

  /**
   * Registers a new user and returns a JWT token.
   *
   * @param request registration data (email and password)
   * @return authentication response with JWT token
   * @throws br.com.projetojava.shared.exception.http4xx.ConflictException if email already exists
   */
  AuthResponse register(RegisterRequest request);

  /**
   * Authenticates a user with email and password.
   *
   * @param request login credentials
   * @return authentication response with JWT token
   * @throws br.com.projetojava.shared.exception.http4xx.UnauthorizedException if credentials are
   *     invalid
   */
  AuthResponse login(LoginRequest request);
}
