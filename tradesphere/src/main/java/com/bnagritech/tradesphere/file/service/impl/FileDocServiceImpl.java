package com.bnagritech.tradesphere.file.service.impl;
import com.bnagritech.tradesphere.file.model.FileDocuments;
import com.bnagritech.tradesphere.file.repository.FileRepository;
import com.bnagritech.tradesphere.file.service.FileDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
    public FileDocuments uploadFile(MultipartFile file, String module, String referenceId )
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }
        String extension = "";
        int lastDotIndex = originalFileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = originalFileName.substring(lastDotIndex);
        }
        String generatedFileName = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(generatedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        try {
            FileDocuments fileDocument = new FileDocuments();
            fileDocument.setFileId(UUID.randomUUID().toString());
            fileDocument.setOriginalFileName(originalFileName);
            fileDocument.setStoredFileName(generatedFileName);
            fileDocument.setFileSize(file.getSize());
            fileDocument.setContentType(file.getContentType());
            fileDocument.setStorageKey(generatedFileName);
            fileDocument.setFileUrl("/api/files/" + fileDocument.getFileId());
            fileDocument.setModule(module);
            fileDocument.setReferenceId(referenceId);
            fileDocument.setUploadedAt(LocalDateTime.now());

            return fileRepository.save(fileDocument);
        }
        catch (Exception exception) {
            Files.deleteIfExists(filePath);
            throw exception;
        }
    }
    @Override
    public FileDocuments getFileById(String fileId) {
        return fileRepository.findByFileId(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));
    }
    @Override
    public List<FileDocuments> getFilesByModuleAndReferenceId(String module, String referenceId) {
        return fileRepository.findByModuleAndReferenceId(module, referenceId);
    }
    @Override
    public void deleteFile(String fileId) throws IOException {
        FileDocuments fileDocument = fileRepository.findByFileId(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found with ID: " + fileId));
        Path uploadPath = Paths.get(uploadDir);
        Path filePath = uploadPath.resolve(fileDocument.getStorageKey());
        if (Files.exists(filePath)) {
            Files.deleteIfExists(filePath);
        }
        fileRepository.delete(fileDocument);
    }

}

