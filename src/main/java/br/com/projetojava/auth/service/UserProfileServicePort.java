package br.com.projetojava.auth.service;

import br.com.projetojava.auth.dto.UserResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contract for authenticated user profile use cases.
 *
 * <p>Consumers depend on this interface, never on the concrete implementation, respecting the
 * <em>Dependency Inversion Principle</em>.
 */
public interface UserProfileServicePort {

  /**
   * Retrieves the profile of the given user.
   *
   * @param userId identifier of the user
   * @return profile response, including avatar info if set
   * @throws br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException if the user does
   *     not exist
   */
  UserResponse getProfile(Long userId);

  /**
   * Uploads a new avatar for the given user and sets it as their current avatar.
   *
   * <p>The previous avatar upload, if any, is kept in {@code file_uploads} as history — only the
   * pointer on {@code users.avatar_id} changes.
   *
   * @param userId identifier of the user
   * @param file multipart image file (JPEG or PNG, per the shared upload validation rules)
   * @return updated profile response with the new avatar
   * @throws br.com.projetojava.shared.exception.http4xx.BadRequestException if the file type is not
   *     allowed or the file exceeds the size limit
   * @throws br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException if the user does
   *     not exist
   */
  UserResponse uploadAvatar(Long userId, MultipartFile file);
}
