package com.interview.quiz2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ประมวลผล Batch ขนาดใหญ่โดยไม่โหลดข้อมูลทั้งหมดเข้า Memory
 *
 * วิธีรัน: java -cp out com.interview.quiz2.Main [file] [batchSize] [rowsToGenerate]
 * ถ้ายังไม่มีไฟล์ โปรแกรมจะสร้างไฟล์ตัวอย่างให้ก่อน (ค่าเริ่มต้น 1,000,000 บรรทัด)
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Path file = Paths.get(args.length > 0 ? args[0] : "metadata.csv");
        int batchSize = args.length > 1 ? Integer.parseInt(args[1]) : 1000;
        long rows = args.length > 2 ? Long.parseLong(args[2]) : 1_000_000L;

        if (!Files.exists(file)) {
            System.out.printf("Generating sample file %s (%,d rows)...%n", file, rows);
            SampleFileGenerator.generate(file, rows);
        }
        System.out.printf("File size: %,d bytes%n", Files.size(file));

        FakeBatchRepository repository = new FakeBatchRepository(false);
        StreamingBatchProcessor processor = new StreamingBatchProcessor(batchSize, repository);

        long start = System.currentTimeMillis();
        StreamingBatchProcessor.ProcessResult result = processor.process(file);
        long elapsed = System.currentTimeMillis() - start;

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);

        System.out.println();
        System.out.println("=== Summary ===");
        System.out.printf("Batch size      : %,d%n", batchSize);
        System.out.printf("Lines read      : %,d%n", result.getTotalLines());
        System.out.printf("Invalid lines   : %,d%n", result.getInvalidLines());
        System.out.printf("Records saved   : %,d%n", result.getSavedRecords());
        System.out.printf("Batches         : %,d%n", result.getBatches());
        System.out.printf("Elapsed         : %,d ms%n", elapsed);
        System.out.printf("Heap used (now) : %,d MB%n", usedMb);

        if (!result.getErrorSamples().isEmpty()) {
            System.out.println();
            System.out.println("=== Invalid line samples (max 10) ===");
            for (String err : result.getErrorSamples()) {
                System.out.println("[INVALID] " + err);
            }
        }
    }
}
