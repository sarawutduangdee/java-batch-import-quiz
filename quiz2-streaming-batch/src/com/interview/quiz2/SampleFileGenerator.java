package com.interview.quiz2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * สร้างไฟล์ metadata ตัวอย่างขนาดใหญ่ (เขียนทีละบรรทัด ไม่สร้าง List ใน memory)
 * ใส่บรรทัดเสียไว้ทุก ๆ 10,000 บรรทัด เพื่อทดสอบการจัดการ error
 */
public final class SampleFileGenerator {

    private static final String[] TYPES = {"INVOICE", "CONTRACT", "RECEIPT", "REPORT"};

    private SampleFileGenerator() {
    }

    public static void generate(Path file, long rows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("documentId,fileName,documentType,fileSize");
            writer.newLine();
            for (long i = 1; i <= rows; i++) {
                if (i % 10_000 == 0) {
                    writer.write("DOC-" + i + ",broken_line");
                } else {
                    writer.write(String.format("DOC-%d,file_%d.pdf,%s,%d",
                            i, i, TYPES[(int) (i % TYPES.length)], 1024 + (i % 5000)));
                }
                writer.newLine();
            }
        }
    }
}
