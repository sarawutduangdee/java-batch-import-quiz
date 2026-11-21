package com.interview.quiz3;

/**
 * เอกสารที่จะ import
 * externalId คือ key ที่ไม่ซ้ำกันของเอกสาร ใช้ตรวจสอบว่าปลายทางมีเอกสารนี้แล้วหรือยัง (idempotency key)
 */
public class ImportDocument {

    private final String externalId;
    private final String fileName;

    public ImportDocument(String externalId, String fileName) {
        this.externalId = externalId;
        this.fileName = fileName;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getFileName() {
        return fileName;
    }
}
