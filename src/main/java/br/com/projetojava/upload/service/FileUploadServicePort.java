package br.com.projetojava.upload.service;

import br.com.projetojava.upload.dto.FileUploadResponse;
import java.util.List;
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
   * Stores an uploaded file and persists its metadata.
   *
   * @param file multipart file from the HTTP request
   * @param productId ID of the product this file belongs to (nullable)
   * @param userId ID of the authenticated user (nullable)
   * @return response DTO with file metadata
   * @throws br.com.projetojava.shared.exception.http4xx.BadRequestException if the file type is not
   *     allowed or the file exceeds the size limit
   */
  FileUploadResponse store(MultipartFile file, Long productId, Long userId);

  /**
   * Stores multiple uploaded files and persists their metadata.
   *
   * <p>Used for product photos, where several images may belong to the same product.
   *
   * @param files multipart files from the HTTP request
   * @param productId ID of the product these files belong to (nullable)
   * @param userId ID of the authenticated user (nullable)
   * @return response DTOs with each file's metadata, in the same order as {@code files}
   * @throws br.com.projetojava.shared.exception.http4xx.BadRequestException if any file is empty,
   *     has a disallowed type, or exceeds the size limit
   */
  List<FileUploadResponse> storeAll(List<MultipartFile> files, Long productId, Long userId);

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
