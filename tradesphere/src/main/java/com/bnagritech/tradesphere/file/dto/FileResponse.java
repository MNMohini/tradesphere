package com.bnagritech.tradesphere.file.dto;

import com.bnagritech.tradesphere.common.enums.FileCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Details of an uploaded file. Other modules store the id (or the url) of the file. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {

    private String id;

    // Relative address that opens the file for a logged-in user, e.g. /api/files/6743a...
    private String url;

    private String originalName;
    private String contentType;
    private long sizeBytes;
    private FileCategory category;
    private String uploadedBy;
    private String uploadedByName;
    private Instant uploadedAt;
}
