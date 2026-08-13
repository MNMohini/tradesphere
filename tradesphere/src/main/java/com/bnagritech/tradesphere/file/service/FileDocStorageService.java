package com.bnagritech.tradesphere.file.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileDocStorageService {
    String store(MultipartFile file, String module, String referenceId)
            throws IOException;

    Resource load(String storageKey);

    void delete(String storageKey) throws IOException;
}
