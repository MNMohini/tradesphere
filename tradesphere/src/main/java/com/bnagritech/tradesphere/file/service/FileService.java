package com.bnagritech.tradesphere.file.service;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.FileCategory;
import com.bnagritech.tradesphere.file.dto.FileResponse;
import com.bnagritech.tradesphere.file.model.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Uploads and downloads. Only JPEG, PNG and WebP images are accepted (and PDF for the DOCUMENT category).
 * The type is checked from the file's own bytes, so a renamed file is refused.
 */
public interface FileService {

    /** A stored file together with its content, for the download endpoint. */
    record FileDownload(StoredFile file, Resource resource) {
    }

    FileResponse upload(MultipartFile file, FileCategory category, User currentUser);

    /** Up to 10 files at once. All must be valid, otherwise nothing is saved. */
    List<FileResponse> uploadMultiple(List<MultipartFile> files, FileCategory category, User currentUser);

    /** Details of a file, if the caller may open it. */
    FileResponse getInfo(String fileId, User currentUser);

    /** The content of a file, if the caller may open it. */
    FileDownload download(String fileId, User currentUser);

    /** The uploader (or an admin) removes a file. */
    void delete(String fileId, User currentUser);

    /** The caller's own uploads, newest first. Every filter is optional; limit defaults to 50 (maximum 200). */
    List<FileResponse> listMine(FileCategory category, Integer limit, User currentUser);
}
