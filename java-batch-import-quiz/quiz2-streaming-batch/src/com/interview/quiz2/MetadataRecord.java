package com.interview.quiz2;

/**
 * Metadata ของเอกสาร 1 บรรทัด
 */
public class MetadataRecord {

    private final String documentId;
    private final String fileName;
    private final String documentType;
    private final long fileSize;

    public MetadataRecord(String documentId, String fileName, String documentType, long fileSize) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.documentType = documentType;
        this.fileSize = fileSize;
    }

    /**
     * แปลง 1 บรรทัด CSV: documentId,fileName,documentType,fileSize
     *
     * @throws IllegalArgumentException เมื่อข้อมูลไม่ถูกต้อง
     */
    public static MetadataRecord parse(String line) {
        String[] cols = line.split(",", -1);
        if (cols.length != 4) {
            throw new IllegalArgumentException("expected 4 columns but got " + cols.length);
        }
        String id = cols[0].trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("documentId is required");
        }
        long size;
        try {
            size = Long.parseLong(cols[3].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("fileSize is not a number");
        }
        return new MetadataRecord(id, cols[1].trim(), cols[2].trim(), size);
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public long getFileSize() {
        return fileSize;
    }
}
