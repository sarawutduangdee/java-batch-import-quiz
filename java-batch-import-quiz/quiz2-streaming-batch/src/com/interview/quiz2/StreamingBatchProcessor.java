package com.interview.quiz2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * อ่านไฟล์ทีละบรรทัด (streaming) แล้วส่งไปบันทึกครั้งละ batchSize รายการ
 *
 * memory ที่ใช้ขึ้นกับ batchSize ไม่ขึ้นกับขนาดไฟล์
 * เพราะในแต่ละช่วงเวลาจะถือ record ไว้ไม่เกิน batchSize รายการ
 */
public class StreamingBatchProcessor {

    /** เก็บตัวอย่างบรรทัดเสียไว้แค่จำนวนนี้ เพื่อไม่ให้ memory โตตามจำนวน error */
    private static final int MAX_ERROR_SAMPLES = 10;

    private final int batchSize;
    private final BatchRepository repository;

    public StreamingBatchProcessor(int batchSize, BatchRepository repository) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be > 0");
        }
        this.batchSize = batchSize;
        this.repository = repository;
    }

    public ProcessResult process(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return process(reader);
        }
    }

    public ProcessResult process(Reader input) throws IOException {
        ProcessResult result = new ProcessResult();
        BufferedReader reader = input instanceof BufferedReader
                ? (BufferedReader) input : new BufferedReader(input);

        List<MetadataRecord> buffer = new ArrayList<>(batchSize);
        String line;
        long lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (lineNumber == 1 || line.trim().isEmpty()) {
                continue; // header หรือบรรทัดว่าง
            }
            result.totalLines++;
            try {
                buffer.add(MetadataRecord.parse(line));
            } catch (IllegalArgumentException e) {
                result.invalidLines++;
                if (result.errorSamples.size() < MAX_ERROR_SAMPLES) {
                    result.errorSamples.add("line " + lineNumber + ": " + e.getMessage());
                }
                continue;
            }
            if (buffer.size() >= batchSize) {
                buffer = flush(buffer, result);
            }
        }

        // อย่าลืม batch สุดท้ายที่ยังไม่ครบ batchSize
        if (!buffer.isEmpty()) {
            flush(buffer, result);
        }
        return result;
    }

    /**
     * ส่ง batch ไปบันทึก แล้วคืน buffer ใหม่
     * ใช้ list ใหม่แทน clear() เผื่อ repository เก็บ reference ของ list เดิมไว้
     */
    private List<MetadataRecord> flush(List<MetadataRecord> buffer, ProcessResult result) {
        repository.saveAll(buffer);
        result.savedRecords += buffer.size();
        result.batches++;
        return new ArrayList<>(batchSize);
    }

    public static class ProcessResult {
        long totalLines;
        long invalidLines;
        long savedRecords;
        long batches;
        final List<String> errorSamples = new ArrayList<>();

        public long getTotalLines() {
            return totalLines;
        }

        public long getInvalidLines() {
            return invalidLines;
        }

        public long getSavedRecords() {
            return savedRecords;
        }

        public long getBatches() {
            return batches;
        }

        public List<String> getErrorSamples() {
            return errorSamples;
        }
    }
}
