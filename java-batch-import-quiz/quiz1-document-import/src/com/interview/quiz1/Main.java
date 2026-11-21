package com.interview.quiz1;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Mini Document Import Batch
 *
 * วิธีรัน: java -cp out com.interview.quiz1.Main [documents.csv] [maxRetry]
 */
public class Main {

    private static final int DEFAULT_MAX_RETRY = 3;

    public static void main(String[] args) throws IOException {
        Path file = Paths.get(args.length > 0 ? args[0] : "documents.csv");
        int maxRetry = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_MAX_RETRY;

        // 1-2. อ่านไฟล์และแปลงเป็น model
        DocumentParser parser = new DocumentParser();
        List<Document> documents = parser.parse(file);

        DocumentImportService service = new DocumentImportService(maxRetry);
        int success = 0;
        int failed = 0;
        int skipped = 0;

        System.out.println("=== Import result (maxRetry=" + maxRetry + ") ===");
        for (Document doc : documents) {
            // 3. เลือกรายการที่มีสิทธิ์ประมวลผล
            if (!service.isEligible(doc)) {
                skipped++;
                System.out.printf("[SKIP]    %s | %s%n", doc, service.skipReason(doc));
                continue;
            }
            // 4. จำลองการ import และเปลี่ยนสถานะ
            DocumentStatus before = doc.getStatus();
            service.importDocument(doc);

            // 5. แสดงผลรายรายการ
            if (doc.getStatus() == DocumentStatus.SUCCESS) {
                success++;
                System.out.printf("[SUCCESS] %s | %s -> %s%n", doc, before, doc.getStatus());
            } else {
                failed++;
                System.out.printf("[FAILED]  %s | %s -> %s, reason: %s%n", doc, before, doc.getStatus(), doc.getMessage());
            }
        }

        if (!parser.getInvalidLines().isEmpty()) {
            System.out.println();
            System.out.println("=== Invalid lines ===");
            for (String invalid : parser.getInvalidLines()) {
                System.out.println("[INVALID] " + invalid);
            }
        }

        // 5. สรุปผล
        System.out.println();
        System.out.println("=== Summary ===");
        System.out.println("Valid records  : " + documents.size());
        System.out.println("Invalid lines  : " + parser.getInvalidLines().size());
        System.out.println("Processed      : " + (success + failed));
        System.out.println("  Success      : " + success);
        System.out.println("  Failed       : " + failed);
        System.out.println("Skipped        : " + skipped);
    }
}
