package com.bnagritech.tradesphere.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    private String fileId;
    private String originalFileName;
    private String storedFileName;
    private Long fileSize;
    private String contentType;
    private String fileUrl;
    private String module;
    private String referenceId;
    private String uploadedBy;
    private LocalDateTime uploadedAt;

}
