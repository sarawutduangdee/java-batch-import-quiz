package com.interview.quiz2;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Test แบบไม่ใช้ JUnit (โจทย์กำหนดให้ใช้ Standard Library เท่านั้น)
 * วิธีรัน: java -cp out com.interview.quiz2.StreamingBatchProcessorTest
 */
public class StreamingBatchProcessorTest {

    private static int passed;

    public static void main(String[] args) throws IOException {
        lastPartialBatchIsFlushed();
        exactMultipleOfBatchSize();
        invalidLinesAreSkipped();
        emptyFileSavesNothing();
        System.out.println("All " + passed + " tests passed");
    }

    /** repository ที่บันทึกขนาดของแต่ละ batch ไว้ตรวจสอบ */
    static class RecordingRepository implements BatchRepository {
        final List<Integer> batchSizes = new ArrayList<>();

        @Override
        public void saveAll(List<MetadataRecord> batch) {
            batchSizes.add(batch.size());
        }
    }

    static void lastPartialBatchIsFlushed() throws IOException {
        RecordingRepository repo = new RecordingRepository();
        new StreamingBatchProcessor(3, repo).process(new StringReader(csv(7)));
        check(repo.batchSizes.toString().equals("[3, 3, 1]"), "batches should be [3, 3, 1] but was " + repo.batchSizes);
    }

    static void exactMultipleOfBatchSize() throws IOException {
        RecordingRepository repo = new RecordingRepository();
        new StreamingBatchProcessor(3, repo).process(new StringReader(csv(6)));
        check(repo.batchSizes.toString().equals("[3, 3]"), "no empty batch expected but was " + repo.batchSizes);
    }

    static void invalidLinesAreSkipped() throws IOException {
        RecordingRepository repo = new RecordingRepository();
        String input = "header\nD1,a.pdf,INV,10\nbad line\nD2,b.pdf,INV,abc\nD3,c.pdf,INV,30\n";
        StreamingBatchProcessor.ProcessResult r = new StreamingBatchProcessor(10, repo).process(new StringReader(input));
        check(r.getTotalLines() == 4, "total lines should be 4");
        check(r.getInvalidLines() == 2, "invalid lines should be 2");
        check(r.getSavedRecords() == 2, "saved records should be 2");
    }

    static void emptyFileSavesNothing() throws IOException {
        RecordingRepository repo = new RecordingRepository();
        new StreamingBatchProcessor(3, repo).process(new StringReader("header\n"));
        check(repo.batchSizes.isEmpty(), "no batch expected for empty file");
    }

    private static String csv(int rows) {
        StringBuilder sb = new StringBuilder("documentId,fileName,documentType,fileSize\n");
        for (int i = 1; i <= rows; i++) {
            sb.append("D").append(i).append(",f.pdf,INV,").append(i).append('\n');
        }
        return sb.toString();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        passed++;
    }
}
