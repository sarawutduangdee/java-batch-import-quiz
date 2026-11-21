package com.interview.quiz1;

/**
 * Model ของเอกสาร 1 รายการ
 */
public class Document {

    private final String id;
    private final String fileName;
    private final String documentType;
    private DocumentStatus status;
    private int retryCount;
    private String message;

    public Document(String id, String fileName, String documentType,
                    DocumentStatus status, int retryCount) {
        this.id = id;
        this.fileName = fileName;
        this.documentType = documentType;
        this.status = status;
        this.retryCount = retryCount;
    }

    public void markSuccess() {
        this.status = DocumentStatus.SUCCESS;
        this.message = "imported";
    }

    public void markFailed(String reason) {
        this.status = DocumentStatus.FAILED;
        this.retryCount++;
        this.message = reason;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("%-8s %-22s %-10s %-8s retry=%d", id, fileName, documentType, status, retryCount);
    }
}
