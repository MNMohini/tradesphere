package com.bnagritech.tradesphere.imagekit.service;

import com.bnagritech.tradesphere.imagekit.dto.ImageKitAuthResponse;
import com.bnagritech.tradesphere.imagekit.dto.ImageKitFile;
import com.bnagritech.tradesphere.imagekit.dto.MediaFolder;
import com.bnagritech.tradesphere.imagekit.dto.MediaRef;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageKitService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/heic");
    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;

    private final RestClient imageKitRestClient;

    // ---- values from application.properties ----

    @Value("${app.imagekit.public-key}")
    private String publicKey;
    @Value("${app.imagekit.private-key}")
    private String privateKey;
    @Value("${app.imagekit.url-endpoint}")          // e.g. https://ik.imagekit.io/bnagritech
    private String urlEndpoint;
    // Leave empty to store media folders directly at the ImageKit root.
    @Value("${app.imagekit.root-folder:}")
    private String rootFolder;

    // How long a client upload signature stays valid. ImageKit allows less than 1 hour.
    @Value("${app.imagekit.auth-expiry-seconds:1800}")
    private int authExpirySeconds;

    @Value("${app.imagekit.upload-base-url:https://upload.imagekit.io/api/v1}")
    private String uploadBaseUrl;

    @Value("${app.imagekit.api-base-url:https://api.imagekit.io/v1}")
    private String apiBaseUrl;

    /** Stop the app at startup if a key is missing, instead of failing on the first upload. */
    @PostConstruct
    void checkSettings() {
        requireSet("app.imagekit.public-key", publicKey);
        requireSet("app.imagekit.private-key", privateKey);
        requireSet("app.imagekit.url-endpoint", urlEndpoint);
        if (authExpirySeconds < 60 || authExpirySeconds > 3500) {
            throw new IllegalStateException("app.imagekit.auth-expiry-seconds must be between 60 and 3500");
        }
    }

    private static void requireSet(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not set in application.properties");
        }
    }

    // =====================================================================
    // 1. Client-side upload (mobile app uploads straight to ImageKit)
    // =====================================================================

    /** Signs one upload. The app calls this, then uploads the file to ImageKit with these values. */
    public ImageKitAuthResponse createUploadAuth(MediaFolder folder) {
        String token = UUID.randomUUID().toString();
        long expire = Instant.now().getEpochSecond() + authExpirySeconds;
        String signature = hmacSha1Hex(privateKey, token + expire);

        return ImageKitAuthResponse.builder()
                .token(token)
                .expire(expire)
                .signature(signature)
                .publicKey(publicKey)
                .urlEndpoint(urlEndpoint)
                .folder(folderPath(folder))
                .build();
    }

    /**
     * After a client-side upload the app sends us only the fileId. We fetch the file from ImageKit
     * ourselves, so a fake URL or a file from another folder is rejected.
     */
    public MediaRef verifyUploadedFile(String fileId, MediaFolder expectedFolder, String userId) {
        if (fileId == null || fileId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fileId is required");
        }
        ImageKitFile file = getFileDetails(fileId.trim());
        String expectedPrefix = folderPath(expectedFolder) + "/";
        if (file.getFilePath() == null || !file.getFilePath().startsWith(expectedPrefix)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File is not in the " + expectedFolder + " folder");
        }
        return toMediaRef(file, userId);
    }

    // =====================================================================
    // 2. Server-side upload (web admin panel, or when the app sends the file to us)
    // =====================================================================

    public MediaRef upload(MultipartFile file, MediaFolder folder, List<String> tags, String userId) {
        validateImage(file);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("fileName", safeFileName(file.getOriginalFilename()));
        body.add("folder", folderPath(folder));
        body.add("useUniqueFileName", "true");
        if (tags != null && !tags.isEmpty()) {
            body.add("tags", String.join(",", tags));
        }

        try {
            ImageKitFile uploaded = imageKitRestClient.post()
                    .uri(uploadBaseUrl + "/files/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(ImageKitFile.class);
            return toMediaRef(uploaded, userId);
        } catch (RestClientException e) {
            log.error("ImageKit upload failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Image upload failed. Please try again.");
        }
    }

    // =====================================================================
    // 3. File management
    // =====================================================================

    public ImageKitFile getFileDetails(String fileId) {
        try {
            return imageKitRestClient.get()
                    .uri(apiBaseUrl + "/files/{fileId}/details", fileId)
                    .retrieve()
                    .body(ImageKitFile.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file not found: " + fileId);
        } catch (RestClientException e) {
            log.error("ImageKit file lookup failed for {}: {}", fileId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not reach the image service");
        }
    }

    /** Deletes permanently. The CDN may keep serving a cached copy until it expires or is purged. */
    public void delete(String fileId) {
        try {
            imageKitRestClient.delete()
                    .uri(apiBaseUrl + "/files/{fileId}", fileId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("ImageKit file {} was already gone", fileId);
        } catch (RestClientException e) {
            log.error("ImageKit delete failed for {}: {}", fileId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not delete the image");
        }
    }

    // =====================================================================
    // 4. URLs with transformations (resize on the fly, no extra storage)
    // =====================================================================

    /** e.g. url("/tradesphere/visits/a.jpg", "w-300,h-300,c-at_max,q-70") */
    public String url(String filePath, String transformation) {
        String base = urlEndpoint.replaceAll("/+$", "");
        if (transformation == null || transformation.isBlank()) {
            return base + filePath;
        }
        return base + "/tr:" + transformation + filePath;
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private String folderPath(MediaFolder folder) {
        String root = rootFolder.replaceAll("/+$", "");
        return root + "/" + folder.path();
    }

    private MediaRef toMediaRef(ImageKitFile f, String userId) {
        return MediaRef.builder()
                .fileId(f.getFileId())
                .filePath(f.getFilePath())
                .url(f.getUrl())
                .thumbnailUrl(f.getThumbnailUrl())
                .size(f.getSize())
                .width(f.getWidth())
                .height(f.getHeight())
                .uploadedBy(userId)
                .uploadedAt(Instant.now())
                .build();
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is larger than expected");
        }
        if (file.getContentType() == null || !ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPEG, PNG, WEBP or HEIC images are allowed");
        }
    }

    /** ImageKit allows letters, digits and . - _ ; anything else becomes _ */
    private String safeFileName(String original) {
        String name = (original == null || original.isBlank()) ? "image.jpg" : original;
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String hmacSha1Hex(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign ImageKit upload", e);
        }
    }
}
