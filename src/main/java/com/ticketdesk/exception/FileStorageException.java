package com.ticketdesk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileStorageException extends RuntimeException {
    public FileStorageException(final String message) {
        super(message);
    }

    public FileStorageException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
