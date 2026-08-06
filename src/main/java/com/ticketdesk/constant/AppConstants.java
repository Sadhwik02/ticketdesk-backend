package com.ticketdesk.constant;

/**
 * Application-wide string constants.
 */
public final class AppConstants {

    private AppConstants() {}

    // ===== Pagination defaults =====
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE   = "10";
    public static final String DEFAULT_SORT_BY     = "createdAt";
    public static final String DEFAULT_SORT_DIR    = "desc";
    public static final int    MAX_PAGE_SIZE       = 100;

    // ===== Ticket number format =====
    public static final String TICKET_NUMBER_PREFIX = "TKT";
    public static final String TICKET_NUMBER_FORMAT = "%s-%d-%06d";

    // ===== Password reset token =====
    public static final long PASSWORD_RESET_TOKEN_EXPIRY_MS = 3_600_000L; // 1 hour

    // ===== File upload =====
    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;   // 10 MB
    public static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/png", "application/pdf"
    };
    public static final String[] ALLOWED_EXTENSIONS = {
            "jpg", "jpeg", "png", "pdf"
    };
}
