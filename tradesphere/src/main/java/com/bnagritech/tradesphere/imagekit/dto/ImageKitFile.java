package com.bnagritech.tradesphere.imagekit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Fields we use from ImageKit's upload and file-details responses. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageKitFile {
    private String fileId;
    private String name;
    private String filePath;
    private String url;
    private String thumbnailUrl;
    private String fileType;
    private Long size;
    private Integer width;
    private Integer height;
}
