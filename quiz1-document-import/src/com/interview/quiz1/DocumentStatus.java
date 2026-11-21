package com.interview.quiz1;

/**
 * สถานะของเอกสารที่รอ import
 */
public enum DocumentStatus {
    PENDING,
    FAILED,
    SUCCESS;

    public static DocumentStatus from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("status is required");
        }
        return DocumentStatus.valueOf(value.trim().toUpperCase());
    }
}
