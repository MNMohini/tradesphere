package com.bnagritech.tradesphere.file.repository;

import com.bnagritech.tradesphere.file.model.FileDocuments;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileRepository extends MongoRepository<FileDocuments,String> {
}
