package com.interview.quiz1;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * เลือกรายการที่มีสิทธิ์ประมวลผล และจำลองการ import
 *
 * กติกาการเลือก:
 *  - PENDING  -> ประมวลผล
 *  - FAILED   -> ประมวลผลเฉพาะเมื่อ retryCount < maxRetry
 *  - SUCCESS  -> ข้าม (import ไปแล้ว)
 *
 * กติกาการจำลอง import (แทนระบบเอกสารจริง):
 *  - fileName ต้องไม่ว่าง และนามสกุลต้องอยู่ใน ALLOWED_EXTENSIONS
 *  - documentType ต้องไม่ว่าง
 */
public class DocumentImportService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            new HashSet<>(Arrays.asList("pdf", "tif", "tiff", "jpg", "png"));

    private final int maxRetry;

    public DocumentImportService(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public boolean isEligible(Document doc) {
        switch (doc.getStatus()) {
            case PENDING:
                return true;
            case FAILED:
                return doc.getRetryCount() < maxRetry;
            default:
                return false;
        }
    }

    public String skipReason(Document doc) {
        if (doc.getStatus() == DocumentStatus.SUCCESS) {
            return "already imported";
        }
        return "retry limit reached (" + doc.getRetryCount() + "/" + maxRetry + ")";
    }

    /**
     * จำลองการ import และเปลี่ยนสถานะของเอกสาร
     */
    public void importDocument(Document doc) {
        String error = validate(doc);
        if (error == null) {
            doc.markSuccess();
        } else {
            doc.markFailed(error);
        }
    }

    private String validate(Document doc) {
        String fileName = doc.getFileName();
        if (fileName.isEmpty()) {
            return "fileName is empty";
        }
        int dot = fileName.lastIndexOf('.');
        String ext = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return "unsupported file type '" + ext + "'";
        }
        if (doc.getDocumentType().isEmpty()) {
            return "documentType is empty";
        }
        return null;
    }
}
