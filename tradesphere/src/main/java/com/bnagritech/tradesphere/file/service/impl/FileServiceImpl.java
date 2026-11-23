package com.bnagritech.tradesphere.file.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.FileCategory;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.file.dto.FileResponse;
import com.bnagritech.tradesphere.file.model.StoredFile;
import com.bnagritech.tradesphere.file.repository.StoredFileRepository;
import com.bnagritech.tradesphere.file.service.FileService;
import com.bnagritech.tradesphere.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final int MAX_FILES_PER_CALL = 10;
    private static final int DEFAULT_LIST_LIMIT = 50;
    private static final int MAX_LIST_LIMIT = 200;

    // Roles that may open every file
    private static final Set<UserRole> SEE_ALL_ROLES = EnumSet.of(UserRole.ADMIN, UserRole.NATIONAL_HEAD);

    private final StoredFileRepository fileRepository;
    private final FileStorageService storageService;

    // application.properties:  app.files.max-size-mb=5   (also set spring.servlet.multipart.max-file-size)
    @Value("${app.files.max-size-mb:5}")
    private long maxSizeMb = 5;

    /** The type found from a file's first bytes. */
    private record DetectedType(String contentType, String extension, boolean pdf) {
    }

    /** A file that has been checked and is ready to be saved. */
    private record CheckedFile(byte[] content, DetectedType type, String originalName) {
    }

    // =====================================================================
    // Upload
    // =====================================================================

    @Override
    public FileResponse upload(MultipartFile file, FileCategory category, User currentUser) {
        return save(check(file, category), category, currentUser);
    }

    @Override
    public List<FileResponse> uploadMultiple(List<MultipartFile> files, FileCategory category, User currentUser) {
        if (files == null || files.isEmpty()) {
            throw badRequest("Send at least one file");
        }
        if (files.size() > MAX_FILES_PER_CALL) {
            throw badRequest("Send at most " + MAX_FILES_PER_CALL + " files at a time");
        }
        // Check everything first, so a bad file does not leave the others half saved
        List<CheckedFile> checked = new ArrayList<>();
        for (MultipartFile file : files) {
            checked.add(check(file, category));
        }
        List<FileResponse> responses = new ArrayList<>();
        for (CheckedFile item : checked) {
            responses.add(save(item, category, currentUser));
        }
        return responses;
    }

    private CheckedFile check(MultipartFile file, FileCategory category) {
        if (file == null || file.isEmpty()) {
            throw badRequest("The file is empty");
        }
        long maxBytes = maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "The file is larger than " + maxSizeMb + " MB. Make the photo smaller and try again.");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw badRequest("The file could not be read");
        }

        DetectedType type = detect(content);
        if (type == null || (type.pdf() && !category.isPdfAllowed())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    category.isPdfAllowed()
                            ? "Only JPEG, PNG, WebP images and PDF files are accepted"
                            : "Only JPEG, PNG and WebP images are accepted for " + category);
        }
        return new CheckedFile(content, type, cleanName(file.getOriginalFilename()));
    }

    private FileResponse save(CheckedFile item, FileCategory category, User currentUser) {
        ZonedDateTime now = ZonedDateTime.now(IST);
        // The stored name is made by the server, so nothing the user types can reach the disk path
        String storagePath = String.format("%04d/%02d/%s.%s",
                now.getYear(), now.getMonthValue(), UUID.randomUUID(), item.type().extension());
        storageService.save(storagePath, item.content());

        List<String> userPath = new ArrayList<>();
        if (currentUser.getPath() != null) {
            userPath.addAll(currentUser.getPath());
        }

        try {
            StoredFile stored = fileRepository.save(StoredFile.builder()
                    .originalName(item.originalName())
                    .storagePath(storagePath)
                    .contentType(item.type().contentType())
                    .sizeBytes(item.content().length)
                    .sha256(sha256(item.content()))
                    .category(category)
                    .uploadedBy(currentUser.getId())
                    .uploadedByName(currentUser.getUsername())
                    .userPath(userPath)
                    .uploadedAt(Instant.now())
                    .build());
            return toResponse(stored);
        } catch (RuntimeException e) {
            storageService.delete(storagePath);   // do not leave a file nobody knows about
            throw e;
        }
    }

    // =====================================================================
    // Read / delete
    // =====================================================================

    @Override
    public FileResponse getInfo(String fileId, User currentUser) {
        return toResponse(loadVisible(fileId, currentUser));
    }

    @Override
    public FileDownload download(String fileId, User currentUser) {
        StoredFile file = loadVisible(fileId, currentUser);
        return new FileDownload(file, storageService.load(file.getStoragePath()));
    }

    @Override
    public void delete(String fileId, User currentUser) {
        StoredFile file = fileRepository.findByIdAndDeletedFalse(fileId)
                .filter(f -> canView(f, currentUser))
                .orElseThrow(this::notFound);
        if (!file.getUploadedBy().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the person who uploaded the file can delete it");
        }
        file.setDeleted(true);
        file.setDeletedAt(Instant.now());
        fileRepository.save(file);
        storageService.delete(file.getStoragePath());
    }

    @Override
    public List<FileResponse> listMine(FileCategory category, Integer limit, User currentUser) {
        int max = limit == null ? DEFAULT_LIST_LIMIT : Math.min(Math.max(limit, 1), MAX_LIST_LIMIT);
        PageRequest page = PageRequest.of(0, max);
        List<StoredFile> files = category == null
                ? fileRepository.findByUploadedByAndDeletedFalseOrderByUploadedAtDesc(currentUser.getId(), page)
                : fileRepository.findByUploadedByAndCategoryAndDeletedFalseOrderByUploadedAtDesc(
                        currentUser.getId(), category, page);
        return files.stream().map(this::toResponse).toList();
    }

    // =====================================================================
    // Rules
    // =====================================================================

    /** Public categories: any logged-in user. Private ones: the uploader, the managers above, admin and national head. */
    private boolean canView(StoredFile file, User user) {
        if (file.getCategory() != null && file.getCategory().isVisibleToAllUsers()) {
            return true;
        }
        return file.getUploadedBy().equals(user.getId())
                || SEE_ALL_ROLES.contains(user.getRole())
                || (file.getUserPath() != null && file.getUserPath().contains(user.getId()));
    }

    /** A file the caller may not open looks like a missing file (404), so its existence is not revealed. */
    private StoredFile loadVisible(String fileId, User user) {
        return fileRepository.findByIdAndDeletedFalse(fileId)
                .filter(f -> canView(f, user))
                .orElseThrow(this::notFound);
    }

    /** Finds the real type from the first bytes. The type the app claims is ignored. */
    private DetectedType detect(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return new DetectedType("image/jpeg", "jpg", false);
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return new DetectedType("image/png", "png", false);
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return new DetectedType("image/webp", "webp", false);
        }
        if (b.length >= 5 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-') {
            return new DetectedType("application/pdf", "pdf", true);
        }
        return null;
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private FileResponse toResponse(StoredFile f) {
        return FileResponse.builder()
                .id(f.getId())
                .url("/api/files/" + f.getId())
                .originalName(f.getOriginalName())
                .contentType(f.getContentType())
                .sizeBytes(f.getSizeBytes())
                .category(f.getCategory())
                .uploadedBy(f.getUploadedBy())
                .uploadedByName(f.getUploadedByName())
                .uploadedAt(f.getUploadedAt())
                .build();
    }

    /** Keeps only the file name (no folders), without control characters, at most 150 characters. */
    private String cleanName(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        String cleaned = name.replace('\\', '/');
        cleaned = cleaned.substring(cleaned.lastIndexOf('/') + 1).replaceAll("\\p{Cntrl}", "").trim();
        if (cleaned.isEmpty()) {
            return "file";
        }
        return cleaned.length() > 150 ? cleaned.substring(cleaned.length() - 150) : cleaned;
    }

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
    }
}
