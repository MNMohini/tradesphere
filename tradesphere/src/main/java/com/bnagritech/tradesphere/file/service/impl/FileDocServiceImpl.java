package com.bnagritech.tradesphere.file.service.impl;

import com.bnagritech.tradesphere.file.dto.FileResponse;
import com.bnagritech.tradesphere.file.service.FileDocService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileDocServiceImpl implements FileDocService {


    @Override
    public FileResponse uploadFile(MultipartFile file, String module, String referenceId) {
        return null;
    }

    @Override
    public Resource downloadFile(String fileId) {
        return null;
    }

    @Override
    public List<FileResponse> getFiles(String module, String referenceId) {
        return List.of();
    }

    @Override
    public void deleteFile(String fileId) {

    }
}
