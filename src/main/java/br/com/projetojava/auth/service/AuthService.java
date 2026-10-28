package br.com.projetojava.auth.service;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.auth.dto.AuthResponse;
import br.com.projetojava.auth.dto.LoginRequest;
import br.com.projetojava.auth.dto.RegisterRequest;
import br.com.projetojava.auth.repository.UserRepository;
import br.com.projetojava.auth.security.JwtServicePort;
import br.com.projetojava.shared.exception.http4xx.ConflictException;
import br.com.projetojava.shared.exception.http4xx.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of authentication use cases.
 *
 * <p>Handles user registration and login, delegating token generation to {@link JwtServicePort} and
 * password hashing to {@link PasswordEncoder}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthServicePort {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtServicePort jwtService;

  /**
   * Registers a new user and returns a JWT token.
   *
   * @param request registration data
   * @return authentication response with JWT token
   * @throws ConflictException if the email is already registered
   */
  @Override
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    log.info("Registrando usuário email='{}'.", request.email());

    if (userRepository.existsByEmail(request.email())) {
      throw new ConflictException("E-mail já cadastrado: " + request.email());
    }

    String hash = passwordEncoder.encode(request.password());
    User user = User.create(request.email(), hash, "USER");
    userRepository.save(user);

    String token = jwtService.generateToken(user.getEmail(), user.getRole());
    return new AuthResponse(
        token, user.getEmail(), user.getRole(), jwtService.extractExpiration(token));
  }

  /**
   * Authenticates a user with email and password.
   *
   * @param request login credentials
   * @return authentication response with JWT token
   * @throws UnauthorizedException if credentials are invalid
   */
  @Override
  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    log.info("Tentativa de login para email='{}'.", request.email());

    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas."));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new UnauthorizedException("Credenciais inválidas.");
    }

    String token = jwtService.generateToken(user.getEmail(), user.getRole());
    return new AuthResponse(
        token, user.getEmail(), user.getRole(), jwtService.extractExpiration(token));
  }
}
