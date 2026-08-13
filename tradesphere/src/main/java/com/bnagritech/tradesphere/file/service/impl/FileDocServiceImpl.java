package com.bnagritech.tradesphere.file.service.impl;
import com.bnagritech.tradesphere.file.dto.FileResponse;
import com.bnagritech.tradesphere.file.model.FileDocuments;
import com.bnagritech.tradesphere.file.repository.FileRepository;
import com.bnagritech.tradesphere.file.service.FileDocService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
@Service
public class FileDocServiceImpl implements FileDocService {

    private final FileRepository fileRepository;
    private final Path uploadDirectory;

    public FileDocServiceImpl(FileRepository fileRepository,
                              @Value("${file.upload-dir:uploads}")String uploadDir) {

        this.fileRepository = fileRepository;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirectory);
        }
        catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    @Override
    public FileResponse uploadFile(MultipartFile file, String module, String referenceId) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = "";
            int lastDot = originalFileName.lastIndexOf(".");
            if (lastDot > 0) {
                extension = originalFileName.substring(lastDot);}
            String storedFileName = UUID.randomUUID() + extension;
            Path targetDirectory = uploadDirectory.resolve(module.toLowerCase()).resolve(referenceId);
            Files.createDirectories(targetDirectory);
            Path targetFile = targetDirectory.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            String storageKey = module.toLowerCase() + "/" + referenceId + "/" + storedFileName;

            FileDocuments fileDocument = new FileDocuments();

            fileDocument.setFileId(UUID.randomUUID().toString());
            fileDocument.setOriginalFileName(originalFileName);
            fileDocument.setStoredFileName(storedFileName);
            fileDocument.setFileSize(file.getSize());
            fileDocument.setContentType(file.getContentType());
            fileDocument.setStorageKey(storageKey);
            fileDocument.setFileUrl("/api/files/" + fileDocument.getFileId());
            fileDocument.setModule(module);
            fileDocument.setReferenceId(referenceId);
            fileDocument.setUploadedAt(LocalDateTime.now());

            FileDocuments savedFile = fileRepository.save(fileDocument);
            return mapToResponse(savedFile);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to store file", e);
        }
    }
    @Override
    public Resource getFile(String fileId) {

        FileDocuments fileDocument = fileRepository.findByFileId(fileId)
                        .orElseThrow(() -> new RuntimeException("File not found: " + fileId));
        try
        {
            Path filePath = uploadDirectory.resolve(fileDocument.getStorageKey()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File cannot be read: " + fileId);
            }
            return resource;
        }
        catch (MalformedURLException ex) {
            throw new RuntimeException("Could not load file: " + fileId, ex);
        }
    }

    @Override
    public List<FileResponse> getFiles(
            String module,
            String referenceId) {
        return fileRepository
                .findByModuleAndReferenceId(module, referenceId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFile(String fileId) {

        FileDocuments fileDocument = fileRepository.findByFileId(fileId)
                        .orElseThrow(() -> new RuntimeException("File not found: " + fileId));
        try {
            Path filePath = uploadDirectory.resolve(fileDocument.getStorageKey()).normalize();
            Files.deleteIfExists(filePath);
            fileRepository.delete(fileDocument);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileId, e);
        }
    }
    private FileResponse mapToResponse(
            FileDocuments fileDocument) {

        return FileResponse.builder()
                .fileId(fileDocument.getFileId())
                .originalFileName(fileDocument.getOriginalFileName())
                .storedFileName(fileDocument.getStoredFileName())
                .fileSize(fileDocument.getFileSize())
                .contentType(fileDocument.getContentType())
                .fileUrl(fileDocument.getFileUrl())
                .module(fileDocument.getModule())
                .referenceId(fileDocument.getReferenceId())
                .uploadedBy(fileDocument.getUploadedBy())
                .uploadedAt(fileDocument.getUploadedAt())
                .build();
    }
}
