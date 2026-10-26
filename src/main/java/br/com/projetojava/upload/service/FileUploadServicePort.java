package br.com.projetojava.upload.service;

import br.com.projetojava.upload.dto.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contract for file upload use cases.
 *
 * <p>Consumers depend on this interface, never on the concrete implementation, respecting the
 * <em>Dependency Inversion Principle</em>.
 */
public interface FileUploadServicePort {

  /**
   * Stores an uploaded file and optionally links it to a product and/or a user.
   *
   * @param file multipart file from the HTTP request
   * @param productId ID of the product this file belongs to, or {@code null}
   * @param userId ID of the authenticated user performing the upload, or {@code null}
   * @return response DTO with file metadata
   * @throws br.com.projetojava.shared.exception.http4xx.BadRequestException if the file type is
   *     not allowed or the file exceeds the size limit
   * @throws br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException if the given
   *     product or user ID does not exist
   */
  FileUploadResponse store(MultipartFile file, Long productId, Long userId);

  /**
   * Sets the given file upload as the active avatar for a user.
   *
   * <p>The previous avatar is not deleted — it remains in {@code file_uploads} as history.
   *
   * @param fileId ID of the {@link br.com.projetojava.upload.domain.FileUpload} to use as avatar
   * @param userId ID of the user whose avatar will be updated
   * @throws br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException if the file or
   *     user does not exist
   */
  void setUserAvatar(Long fileId, Long userId);

  /**
   * Loads a stored file as a {@link Resource} for download.
   *
   * @param id file upload record identifier
   * @return the file as a {@link Resource}
   * @throws br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException if the record
   *     does not exist
   */
  Resource load(Long id);
}
