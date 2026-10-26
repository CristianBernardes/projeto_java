package br.com.projetojava.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating and validating JWT tokens.
 *
 * <p>Uses HMAC-SHA256 (HS256) with a secret key configured via {@code app.jwt.secret}. Token
 * expiration is controlled by {@code app.jwt.expiration-ms}.
 */
@Service
public class JwtService {

  private final SecretKey secretKey;
  private final long expirationMs;

  /**
   * Constructs the service with the configured secret and expiration.
   *
   * @param secret JWT signing secret (at least 256 bits for HS256)
   * @param expirationMs token validity in milliseconds
   */
  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-ms}") long expirationMs) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  /**
   * Generates a signed JWT token for the given subject and role.
   *
   * @param subject token subject (typically the user email)
   * @param role user role stored as a claim
   * @return signed JWT string
   */
  public String generateToken(String subject, String role) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
        .subject(subject)
        .claim("role", role)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(secretKey)
        .compact();
  }

  /**
   * Extracts the subject (email) from a JWT token.
   *
   * @param token JWT string
   * @return subject claim value
   */
  public String extractSubject(String token) {
    return parseClaims(token).getSubject();
  }

  /**
   * Extracts the role claim from a JWT token.
   *
   * @param token JWT string
   * @return role claim value
   */
  public String extractRole(String token) {
    return parseClaims(token).get("role", String.class);
  }

  /**
   * Extracts the expiration date/time from a JWT token.
   *
   * @param token JWT string
   * @return expiration as {@link LocalDateTime}
   */
  public LocalDateTime extractExpiration(String token) {
    Date expiry = parseClaims(token).getExpiration();
    return expiry.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  /**
   * Validates whether the token is well-formed, signed correctly and not expired.
   *
   * @param token JWT string
   * @return {@code true} if the token is valid
   */
  public boolean isTokenValid(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}
