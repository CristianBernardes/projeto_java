package br.com.projetojava.auth.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing an application user.
 *
 * <p>Stores authentication credentials and role information. Passwords are stored as BCrypt hashes.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "role", nullable = false)
  private String role;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Creates a new user with the provided credentials.
   *
   * @param email user email (must be unique)
   * @param passwordHash BCrypt-hashed password
   * @param role user role (e.g., "USER", "ADMIN")
   * @return new {@link User} instance
   */
  public static User create(String email, String passwordHash, String role) {
    User user = new User();
    user.email = email;
    user.passwordHash = passwordHash;
    user.role = role;
    user.active = true;
    user.createdAt = LocalDateTime.now();
    user.updatedAt = LocalDateTime.now();
    return user;
  }

  /**
   * Two users are equal if they share the same persisted {@code id}.
   *
   * @param o object to compare
   * @return {@code true} if both have the same non-null id
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User other)) return false;
    return id != null && id.equals(other.id);
  }

  /**
   * Constant hash based on type.
   *
   * @return constant hash for all {@link User} instances
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
