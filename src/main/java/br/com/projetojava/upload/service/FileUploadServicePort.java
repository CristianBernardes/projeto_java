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
   * Stores an uploaded file linked to a product and persists its metadata.
   *
   * @param file multipart file from the HTTP request
   * @param productId ID of the product this file belongs to
   * @param userId ID of the authenticated user performing the upload
   * @return response DTO with file metadata
   * @throws br.com.projetojava.shared.exception.http4xx.BadRequestException if the file type is
   *     not allowed or the file exceeds the size limit
   * @throws br.com.projetojava.shared.exception.http4xx.ResourceNotFoundException if the product
   *     or user does not exist
   */
  FileUploadResponse store(MultipartFile file, Long productId, Long userId);

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
