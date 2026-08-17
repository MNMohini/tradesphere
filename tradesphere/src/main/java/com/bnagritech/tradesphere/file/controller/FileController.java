package com.bnagritech.tradesphere.file.controller;

import com.bnagritech.tradesphere.file.model.FileDocuments;
import com.bnagritech.tradesphere.file.service.FileDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileDocService fileDocService;

    @PostMapping("/upload")
    public ResponseEntity<FileDocuments> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam String module,
            @RequestParam String referenceId
    ) throws IOException {
        return ResponseEntity.ok(
                fileDocService.uploadFile(file, module, referenceId));
    }

    @GetMapping("/module/{module}/{referenceId}")
    public ResponseEntity<List<FileDocuments>>
    getFilesByModuleAndReferenceId(@PathVariable String module, @PathVariable String referenceId) {
        return ResponseEntity.ok(
                fileDocService.getFilesByModuleAndReferenceId(module, referenceId));
    }
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) throws IOException {
        fileDocService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}
