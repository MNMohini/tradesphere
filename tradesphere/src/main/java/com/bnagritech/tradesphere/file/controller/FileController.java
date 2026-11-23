package com.bnagritech.tradesphere.file.controller;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.FileCategory;
import com.bnagritech.tradesphere.file.dto.FileResponse;
import com.bnagritech.tradesphere.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Upload and download of photos and documents. Every endpoint needs a login. */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // POST /api/files      (multipart form: field "file" with the image, field "category", for example OUTLET_PHOTO)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam FileCategory category,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.upload(file, category, currentUser));
    }

    // POST /api/files/multiple     (multipart form: several fields named "files", plus "category")
    @PostMapping(value = "/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileResponse>> uploadMultiple(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam FileCategory category,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.uploadMultiple(files, category, currentUser));
    }

    // GET /api/files/mine?category=VISIT_PHOTO&limit=50
    @GetMapping("/mine")
    public ResponseEntity<List<FileResponse>> listMine(
            @RequestParam(required = false) FileCategory category,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(fileService.listMine(category, limit, currentUser));
    }

    // GET /api/files/{fileId}/info
    @GetMapping("/{fileId}/info")
    public ResponseEntity<FileResponse> getInfo(
            @PathVariable String fileId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(fileService.getInfo(fileId, currentUser));
    }

    // GET /api/files/{fileId}     (the image or PDF itself; send the Bearer token)
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> download(
            @PathVariable String fileId,
            @AuthenticationPrincipal User currentUser) {
        FileService.FileDownload download = fileService.download(fileId, currentUser);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.file().getContentType()))
                .contentLength(download.file().getSizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(download.file().getOriginalName(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=86400")
                .header("X-Content-Type-Options", "nosniff")
                .body(download.resource());
    }

    // DELETE /api/files/{fileId}
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable String fileId,
            @AuthenticationPrincipal User currentUser) {
        fileService.delete(fileId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
