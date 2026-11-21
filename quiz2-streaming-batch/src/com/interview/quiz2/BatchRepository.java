package com.interview.quiz2;

import java.util.List;

/**
 * ปลายทางสำหรับบันทึกข้อมูลเป็น batch
 * ในระบบจริงจะเป็น JDBC (addBatch/executeBatch) หรือ JPA saveAll
 */
public interface BatchRepository {

    void saveAll(List<MetadataRecord> batch);
}
