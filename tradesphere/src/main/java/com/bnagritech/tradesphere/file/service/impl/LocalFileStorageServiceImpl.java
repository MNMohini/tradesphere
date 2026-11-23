package com.bnagritech.tradesphere.file.service.impl;

import com.bnagritech.tradesphere.file.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/** Stores files in a folder on the server. The folder is set with app.files.base-dir. */
@Slf4j
@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    // application.properties:  app.files.base-dir=./uploads
    @Value("${app.files.base-dir:./uploads}")
    private String baseDir = "./uploads";

    private Path root;

    @PostConstruct
    void init() throws IOException {
        root = Paths.get(baseDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
        log.info("Uploaded files are stored in {}", root);
    }

    @Override
    public void save(String storagePath, byte[] content) {
        Path target = resolve(storagePath);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            log.error("Could not save the file {}", target, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The file could not be saved");
        }
    }

    @Override
    public Resource load(String storagePath) {
        Path target = resolve(storagePath);
        if (!Files.isReadable(target)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The file is no longer available");
        }
        return new FileSystemResource(target);
    }

    @Override
    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(resolve(storagePath));
        } catch (IOException e) {
            log.warn("Could not delete the file {}", storagePath, e);
        }
    }

    /** Refuses any path that would leave the storage folder. */
    private Path resolve(String storagePath) {
        Path target = root.resolve(storagePath).normalize();
        if (!target.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }
        return target;
    }
}
