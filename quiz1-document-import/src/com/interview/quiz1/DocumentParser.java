package com.interview.quiz1;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * อ่านไฟล์ CSV แล้วแปลงเป็น {@link Document}
 * รูปแบบ: id,fileName,documentType,status,retryCount (บรรทัดแรกเป็น header)
 * บรรทัดที่ข้อมูลไม่ถูกต้องจะถูกเก็บไว้ใน invalidLines ไม่ทำให้โปรแกรมหยุด
 */
public class DocumentParser {

    private static final int EXPECTED_COLUMNS = 5;

    private final List<String> invalidLines = new ArrayList<>();

    public List<Document> parse(Path file) throws IOException {
        List<Document> documents = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 || line.trim().isEmpty()) {
                    continue; // header หรือบรรทัดว่าง
                }
                try {
                    documents.add(parseLine(line));
                } catch (IllegalArgumentException e) {
                    invalidLines.add("line " + lineNumber + ": " + e.getMessage() + " -> " + line);
                }
            }
        }
        return documents;
    }

    Document parseLine(String line) {
        String[] cols = line.split(",", -1);
        if (cols.length != EXPECTED_COLUMNS) {
            throw new IllegalArgumentException("expected " + EXPECTED_COLUMNS + " columns but got " + cols.length);
        }
        String id = cols[0].trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("id is required");
        }
        DocumentStatus status;
        try {
            status = DocumentStatus.from(cols[3]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown status '" + cols[3].trim() + "'");
        }
        int retryCount;
        try {
            retryCount = Integer.parseInt(cols[4].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("retryCount is not a number");
        }
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount must not be negative");
        }
        return new Document(id, cols[1].trim(), cols[2].trim(), status, retryCount);
    }

    public List<String> getInvalidLines() {
        return invalidLines;
    }
}
