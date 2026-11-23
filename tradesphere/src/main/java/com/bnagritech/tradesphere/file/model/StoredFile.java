package com.bnagritech.tradesphere.file.model;

import com.bnagritech.tradesphere.common.enums.FileCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/** The record of one uploaded file. The file itself is on disk (or in cloud storage later). */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "files")
@CompoundIndex(name = "uploader_time", def = "{'uploadedBy': 1, 'uploadedAt': -1}")
public class StoredFile {

    @Id
    private String id;

    private String originalName;        // the name on the user's phone, cleaned
    private String storagePath;         // where the storage service keeps it, e.g. 2026/11/3f9c....jpg
    private String contentType;         // found from the file's own bytes, not from what the app claimed
    private long sizeBytes;
    private String sha256;              // fingerprint of the content

    private FileCategory category;

    private String uploadedBy;          // user id
    private String uploadedByName;
    private List<String> userPath;      // the uploader's ancestors, so their managers can open private files
    private Instant uploadedAt;

    private boolean deleted;
    private Instant deletedAt;
}
