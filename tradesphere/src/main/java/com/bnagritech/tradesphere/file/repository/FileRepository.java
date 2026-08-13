package com.bnagritech.tradesphere.file.repository;

import com.bnagritech.tradesphere.file.model.FileDocuments;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileRepository extends MongoRepository<FileDocuments,String> {

    List<FileDocuments> findByModuleAndReferenceId(String module, String referenceId);
    List<FileDocuments> findByUploadedBy(String uploadedBy);
}
