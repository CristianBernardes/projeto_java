package br.com.projetojava.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import br.com.projetojava.auth.domain.User;
import br.com.projetojava.auth.dto.AuthResponse;
import br.com.projetojava.auth.dto.LoginRequest;
import br.com.projetojava.auth.dto.RegisterRequest;
import br.com.projetojava.auth.repository.UserRepository;
import br.com.projetojava.auth.security.JwtServicePort;
import br.com.projetojava.shared.exception.http4xx.ConflictException;
import br.com.projetojava.shared.exception.http4xx.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtServicePort jwtService;

  @InjectMocks private AuthService authService;

  @Test
  @DisplayName("register — should create user and return JWT token")
  void register_shouldCreateUserAndReturnToken() {
    RegisterRequest request = new RegisterRequest("user@example.com", "secret123");

    when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
    when(passwordEncoder.encode("secret123")).thenReturn("$2a$hash");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    when(jwtService.generateToken(anyString(), anyString())).thenReturn("jwt-token");
    when(jwtService.extractExpiration("jwt-token")).thenReturn(LocalDateTime.now().plusDays(1));

    AuthResponse response = authService.register(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    assertThat(response.email()).isEqualTo("user@example.com");
    assertThat(response.role()).isEqualTo("USER");
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("register — should throw ConflictException when email already exists")
  void register_shouldThrowConflictWhenEmailExists() {
    RegisterRequest request = new RegisterRequest("existing@example.com", "secret123");
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("existing@example.com");
  }

  @Test
  @DisplayName("login — should return JWT token for valid credentials")
  void login_shouldReturnTokenForValidCredentials() {
    LoginRequest request = new LoginRequest("user@example.com", "secret123");
    User user = User.create("user@example.com", "$2a$hash", "USER");

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("secret123", "$2a$hash")).thenReturn(true);
    when(jwtService.generateToken(anyString(), anyString())).thenReturn("jwt-token");
    when(jwtService.extractExpiration("jwt-token")).thenReturn(LocalDateTime.now().plusDays(1));

    AuthResponse response = authService.login(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    assertThat(response.email()).isEqualTo("user@example.com");
  }

  @Test
  @DisplayName("login — should throw UnauthorizedException for invalid password")
  void login_shouldThrowUnauthorizedForInvalidPassword() {
    LoginRequest request = new LoginRequest("user@example.com", "wrong");
    User user = User.create("user@example.com", "$2a$hash", "USER");

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "$2a$hash")).thenReturn(false);

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("Credenciais inválidas");
  }

  @Test
  @DisplayName("login — should throw UnauthorizedException when user not found")
  void login_shouldThrowUnauthorizedWhenUserNotFound() {
    LoginRequest request = new LoginRequest("unknown@example.com", "secret123");
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request)).isInstanceOf(UnauthorizedException.class);
  }
}
