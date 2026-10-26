package br.com.projetojava.auth.controller;

import br.com.projetojava.auth.dto.AuthResponse;
import br.com.projetojava.auth.dto.LoginRequest;
import br.com.projetojava.auth.dto.RegisterRequest;
import br.com.projetojava.auth.service.AuthServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 *
 * <p>Provides user registration and login, returning JWT tokens on success.
 */
@Tag(name = "Authentication", description = "User registration and login")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthServicePort authService;

  /**
   * Registers a new user account.
   *
   * @param request registration data (email and password)
   * @return {@code 200 OK} with JWT token and user info
   */
  @Operation(
      summary = "Register",
      description = "Creates a new user account and returns a JWT token.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User registered successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Email already registered",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "422",
            description = "Invalid input data",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  /**
   * Authenticates a user and returns a JWT token.
   *
   * @param request login credentials
   * @return {@code 200 OK} with JWT token and user info
   */
  @Operation(
      summary = "Login",
      description = "Authenticates with email and password, returning a JWT token.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      })
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }
}
