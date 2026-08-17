package com.bnagritech.tradesphere.file.service;

import com.bnagritech.tradesphere.file.dto.FileResponse;
import com.bnagritech.tradesphere.file.model.FileDocuments;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileDocService {

    FileDocuments uploadFile(MultipartFile file, String module, String referenceId) throws IOException;
    FileDocuments getFileById(String fileId);
    List<FileDocuments> getFilesByModuleAndReferenceId(String module, String referenceId);
    void deleteFile(String fileId) throws IOException;
}
