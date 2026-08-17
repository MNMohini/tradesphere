package com.bnagritech.tradesphere.file.service.impl;
import com.bnagritech.tradesphere.file.dto.FileResponse;
import com.bnagritech.tradesphere.file.model.FileDocuments;
import com.bnagritech.tradesphere.file.repository.FileRepository;
import com.bnagritech.tradesphere.file.service.FileDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class FileDocServiceImpl implements FileDocService {

    private final FileRepository fileRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public FileDocuments uploadFile(
            MultipartFile file,
            String module,
            String referenceId ) throws IOException {

        // 1. Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // 2. Create upload directory
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 3. Get original file name
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        // 4. Generate unique file name
        String extension = "";

        int lastDotIndex = originalFileName.lastIndexOf(".");

        if (lastDotIndex > 0) {
            extension = originalFileName.substring(lastDotIndex);
        }

        String generatedFileName =
                UUID.randomUUID() + extension;

        // 5. Create physical file path
        Path filePath = uploadPath.resolve(generatedFileName);

        // 6. Save physical file
        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        try {

            // 7. Create MongoDB document
            FileDocuments fileDocument = new FileDocuments();

            fileDocument.setFileId(UUID.randomUUID().toString());
            fileDocument.setOriginalFileName(originalFileName);
            fileDocument.setStoredFileName(fileDocument.getStoredFileName());
            fileDocument.setFileSize(file.getSize());
            fileDocument.setContentType(file.getContentType());
            fileDocument.setStorageKey(fileDocument.getStorageKey());
            fileDocument.setFileUrl("/api/files/" + fileDocument.getFileId());
            fileDocument.setModule(module);
            fileDocument.setReferenceId(referenceId);
            fileDocument.setUploadedAt(LocalDateTime.now());

            // 8. Save metadata in MongoDB
            return fileRepository.save(fileDocument);

        } catch (Exception exception) {

            // 9. If MongoDB save fails,
            // delete the physical file
            Files.deleteIfExists(filePath);

            throw exception;
        }
    }

    @Override
    public Resource getFile(String fileId) {
        return null;
    }

    @Override
    public FileDocuments getFileById(String fileId) {

        return fileRepository.findByFileId(fileId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "File not found with ID: " + fileId
                        )
                );
    }

    @Override
    public List<FileDocuments> getFilesByModuleAndReferenceId(
            String module,
            String referenceId) {

        return fileRepository
                .findByModuleAndReferenceId(module, referenceId);
    }

    @Override
    public void deleteFile(String fileId) throws IOException {

        // 1. Find metadata
        FileDocuments fileDocument =
                fileRepository.findByFileId(fileId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "File not found with ID: " + fileId
                                )
                        );

        // 2. Get physical file path
        Path filePath =
                Paths.get(fileDocument.getFileUrl());

        // 3. Delete physical file
        Files.deleteIfExists(filePath);

        // 4. Delete MongoDB metadata
        fileRepository.delete(fileDocument);
    }

}

