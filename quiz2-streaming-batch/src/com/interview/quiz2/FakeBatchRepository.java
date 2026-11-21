package com.interview.quiz2;

import java.util.List;

/**
 * Repository จำลอง ไม่เชื่อมฐานข้อมูลจริง
 * เก็บแค่ตัวนับ ไม่เก็บ record ไว้ เพื่อไม่ให้ memory โตตามขนาดไฟล์
 */
public class FakeBatchRepository implements BatchRepository {

    private final boolean verbose;
    private long batchCount;
    private long savedCount;

    public FakeBatchRepository(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void saveAll(List<MetadataRecord> batch) {
        batchCount++;
        savedCount += batch.size();
        if (verbose) {
            System.out.printf("  saved batch #%d (%d records, total %d)%n", batchCount, batch.size(), savedCount);
        }
    }

    public long getBatchCount() {
        return batchCount;
    }

    public long getSavedCount() {
        return savedCount;
    }
}
