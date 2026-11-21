package com.interview.quiz3;

public class ImportResult {

    private final String externalId;
    private final ImportStatus status;
    private final int attempts;
    private final String message;

    public ImportResult(String externalId, ImportStatus status, int attempts, String message) {
        this.externalId = externalId;
        this.status = status;
        this.attempts = attempts;
        this.message = message;
    }

    public String getExternalId() {
        return externalId;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("%-8s %-32s attempts=%d  %s", externalId, status, attempts, message);
    }
}
