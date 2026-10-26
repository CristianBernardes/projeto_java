package br.com.projetojava.upload.repository;

import br.com.projetojava.upload.domain.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for the {@link FileUpload} entity.
 *
 * <p>Provides standard CRUD operations for file upload records.
 */
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {}
