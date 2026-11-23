package com.bnagritech.tradesphere.file.repository;

import com.bnagritech.tradesphere.common.enums.FileCategory;
import com.bnagritech.tradesphere.file.model.StoredFile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StoredFileRepository extends MongoRepository<StoredFile, String> {

    /** A deleted file behaves as if it never existed. */
    Optional<StoredFile> findByIdAndDeletedFalse(String id);

    /** The caller's own uploads, newest first. Pass PageRequest.of(0, limit) to cap the list. */
    List<StoredFile> findByUploadedByAndDeletedFalseOrderByUploadedAtDesc(String uploadedBy, Pageable pageable);

    List<StoredFile> findByUploadedByAndCategoryAndDeletedFalseOrderByUploadedAtDesc(
            String uploadedBy, FileCategory category, Pageable pageable);
}
