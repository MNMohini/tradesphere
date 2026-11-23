package com.bnagritech.tradesphere.file.service;

import org.springframework.core.io.Resource;

/**
 * Keeps the bytes of the files. The default implementation uses a folder on the server's disk.
 * To use cloud storage (for example Amazon S3) later, write another implementation of this interface;
 * nothing else in the project changes.
 */
public interface FileStorageService {

    /** Saves the bytes under a path that the service itself chose (it never comes from the user). */
    void save(String storagePath, byte[] content);

    /** Opens a saved file. Throws a 404 if it is not there. */
    Resource load(String storagePath);

    /** Removes a saved file. Does nothing if it is already gone. */
    void delete(String storagePath);
}
