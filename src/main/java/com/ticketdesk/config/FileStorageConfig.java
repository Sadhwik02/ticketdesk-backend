package com.ticketdesk.config;

import com.ticketdesk.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Ensures the file upload directory exists on application startup.
 */
@Slf4j
@Configuration
public class FileStorageConfig {

    private final Path uploadPath;

    public FileStorageConfig(@Value("${app.file.upload-dir}") final String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        initDirectory();
    }

    /**
     * Returns the resolved, absolute upload directory path.
     */
    public Path getUploadPath() {
        return uploadPath;
    }

    private void initDirectory() {
        try {
            Files.createDirectories(uploadPath);
            log.info("File upload directory initialized: {}", uploadPath);
        } catch (final IOException ex) {
            throw new FileStorageException(
                    "Could not create upload directory: " + uploadPath, ex);
        }
    }
}
