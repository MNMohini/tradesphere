package com.bnagritech.tradesphere.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * What we store in MongoDB for an image (embed it in Visit, Outlet, User...).
 * Keep fileId: it is needed to delete the file later. Store the path, not only the URL,
 * so a change of URL endpoint later does not break old records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaRef {
    private String fileId;
    private String filePath;
    private String url;
    private String thumbnailUrl;
    private Long size;
    private Integer width;
    private Integer height;
    private String uploadedBy;
    private Instant uploadedAt;
}
