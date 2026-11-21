package com.interview.quiz3;

import com.interview.quiz3.FakeDocumentGateway.Outcome;

import java.util.ArrayList;
import java.util.List;

/**
 * Batch Import ที่รับมือ Connection Error และ Retry ได้ถูกต้อง
 *
 * วิธีรัน: java -cp out com.interview.quiz3.Main
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        FakeDocumentGateway gateway = new FakeDocumentGateway()
                .script("DOC-001")                                                     // สำเร็จทันที
                .script("DOC-002", Outcome.TEMPORARY_UNAVAILABLE, Outcome.TEMPORARY_UNAVAILABLE) // สำเร็จครั้งที่ 3
                .script("DOC-003", Outcome.INVALID_DATA)                               // ข้อมูลผิด ไม่ retry
                .script("DOC-004", Outcome.TIMEOUT_AFTER_CREATED)                      // timeout แต่สร้างแล้ว -> ห้ามสร้างซ้ำ
                .script("DOC-005", Outcome.TIMEOUT_NOT_CREATED)                        // timeout และยังไม่สร้าง -> สร้างใหม่
                .script("DOC-006", Outcome.CONNECTION_LOST, Outcome.CONNECTION_LOST,
                        Outcome.CONNECTION_LOST)                                       // retry ครบแล้วยังไม่ได้
                .script("DOC-007", Outcome.CONNECTION_LOST)                            // reconnect แล้วสำเร็จ
                .script("DOC-008", Outcome.AUTH_FAILED);                               // รหัสผ่านผิด -> หยุด batch

        List<ImportDocument> documents = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String id = String.format("DOC-%03d", i);
            documents.add(new ImportDocument(id, id.toLowerCase() + ".pdf"));
        }

        RetryPolicy retryPolicy = new RetryPolicy(3, 200, 2.0, 2000, Thread::sleep);
        BatchImporter importer = new BatchImporter(gateway, retryPolicy);

        System.out.println("=== Import (maxAttempts=" + retryPolicy.getMaxAttempts() + ") ===");
        BatchImporter.BatchSummary summary = importer.importAll(documents);

        System.out.println();
        System.out.println("=== Final status ===");
        for (ImportResult r : summary.getResults()) {
            System.out.println(r);
        }

        System.out.println();
        System.out.println("=== Summary ===");
        for (ImportStatus status : ImportStatus.values()) {
            long n = summary.count(status);
            if (n > 0) {
                System.out.printf("%-32s : %d%n", status, n);
            }
        }
        System.out.println("Batch aborted                    : " + summary.isAborted());
        System.out.println("Reconnections                    : " + (gateway.getConnectCount() - 1));
        System.out.println("Duplicate documents at target    : " + gateway.countDuplicates());
    }
}
