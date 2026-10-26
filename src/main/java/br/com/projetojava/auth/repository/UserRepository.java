package br.com.projetojava.auth.repository;

import br.com.projetojava.auth.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for the {@link User} entity.
 *
 * <p>Provides standard CRUD operations and custom queries for user lookup.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user by their email address.
   *
   * @param email the email to search for
   * @return an {@link Optional} containing the user if found
   */
  Optional<User> findByEmail(String email);

  /**
   * Checks whether a user with the given email already exists.
   *
   * @param email the email to check
   * @return {@code true} if a user with this email exists
   */
  boolean existsByEmail(String email);
}
