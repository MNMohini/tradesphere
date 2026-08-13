package com.bnagritech.tradesphere.file.model;

import com.bnagritech.tradesphere.common.enums.Modules;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "files")

public class File {
    @Id
    private String id;
    private String originalFileName;
    private String storedFileName;
    private Long fileSize;
    private String storageKey;
    private String fileUrl;
    private Modules module;
    private String referenceId;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

}
