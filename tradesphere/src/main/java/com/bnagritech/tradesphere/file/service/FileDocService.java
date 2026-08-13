package com.bnagritech.tradesphere.file.service;

import com.bnagritech.tradesphere.file.dto.FileResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileDocService {

    FileResponse uploadFile(MultipartFile file, String module, String referenceId);
    Resource getFile(String fileId);
    List<FileResponse> getFiles(String module, String referenceId);
    void deleteFile(String fileId);
}
