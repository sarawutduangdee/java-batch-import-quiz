# java-batch-import-quiz

คำตอบโจทย์สัมภาษณ์ 3 ข้อ เรื่อง Batch Import เอกสาร

- Java 8 ขึ้นไป
- ใช้ Java Standard Library เท่านั้น (ไม่มี Spring, Database, FileNet หรือ JUnit)
- แต่ละข้อเป็นโปรเจกต์อิสระ มี `Main` ของตัวเอง

```
java-batch-import-quiz/
├── quiz1-document-import/   โจทย์ 1: Mini Document Import Batch
├── quiz2-streaming-batch/   โจทย์ 2: Streaming + Batch โดยไม่โหลดทั้งไฟล์เข้า memory
└── quiz3-retry-import/      โจทย์ 3: Import ที่รับมือ connection error และ retry ได้ถูกต้อง
```

## วิธีรัน

macOS / Linux / Git Bash (รันในโฟลเดอร์ของแต่ละข้อ):

```bash
cd quiz1-document-import
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out com.interview.quiz1.Main documents.csv

cd ../quiz2-streaming-batch
javac -encoding UTF-8 -d out $(find src test -name "*.java")
java -cp out com.interview.quiz2.StreamingBatchProcessorTest
java -Xmx32m -cp out com.interview.quiz2.Main metadata.csv 1000 1000000

cd ../quiz3-retry-import
javac -encoding UTF-8 -d out $(find src test -name "*.java")
java -cp out com.interview.quiz3.BatchImporterTest
java -cp out com.interview.quiz3.Main
```

Windows PowerShell: ใช้คำสั่ง compile นี้แทน (ส่วนคำสั่ง `java` ใช้เหมือนเดิม)

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src,test -Filter *.java).FullName
```

หรือเปิดแต่ละโฟลเดอร์ใน IntelliJ IDEA แล้วตั้ง `src` (และ `test`) เป็น source root

---

## โจทย์ 1: Mini Document Import Batch

ขั้นตอน: อ่านไฟล์ → แปลงเป็น model → เลือกรายการที่มีสิทธิ์ → จำลอง import และเปลี่ยนสถานะ → แสดงผลรายรายการและสรุป

**รูปแบบไฟล์ input** (`documents.csv`)

```
id,fileName,documentType,status,retryCount
DOC-001,invoice_2026_001.pdf,INVOICE,PENDING,0
```

**สมมติฐาน**

| สถานะ | มีสิทธิ์ประมวลผล |
|---|---|
| `PENDING` | ใช่ |
| `FAILED` | ใช่ ถ้า `retryCount < maxRetry` (ค่าเริ่มต้น 3) |
| `SUCCESS` | ไม่ (import แล้ว) |

- การ import จำลองจะสำเร็จเมื่อ `fileName` ไม่ว่าง นามสกุลเป็น pdf/tif/tiff/jpg/png และ `documentType` ไม่ว่าง
- ถ้าไม่สำเร็จ สถานะเป็น `FAILED` และ `retryCount` เพิ่มขึ้น 1
- บรรทัดที่ข้อมูลผิดรูปแบบ (จำนวนคอลัมน์ไม่ครบ, status ไม่รู้จัก, retryCount ไม่ใช่ตัวเลข) จะถูกรายงานแยก ไม่ทำให้โปรแกรมหยุด

| Class | หน้าที่ |
|---|---|
| `Document`, `DocumentStatus` | model |
| `DocumentParser` | อ่านไฟล์และแปลงเป็น model |
| `DocumentImportService` | เลือกรายการและจำลอง import |
| `Main` | แสดงผลและสรุป |

---

## โจทย์ 2: Streaming + Batch

- อ่านไฟล์ทีละบรรทัดด้วย `BufferedReader` ไม่โหลดทั้งไฟล์เข้า `List`
- สะสม record ไว้ใน buffer จนครบ `batchSize` แล้วส่งให้ `BatchRepository.saveAll()` ครั้งเดียว (แทน JDBC `addBatch`/`executeBatch`)
- **flush batch สุดท้าย** ที่ยังไม่ครบ `batchSize` หลังอ่านไฟล์จบ
- ปิดไฟล์ด้วย try-with-resources
- บรรทัดที่เสียจะถูกนับและข้าม เก็บตัวอย่าง error ไว้แค่ 10 รายการ เพื่อไม่ให้ memory โตตามจำนวน error
- `FakeBatchRepository` เก็บแค่ตัวนับ ไม่เก็บ record

ถ้ายังไม่มีไฟล์ โปรแกรมจะสร้างไฟล์ตัวอย่างให้ (ค่าเริ่มต้น 1,000,000 บรรทัด ประมาณ 40 MB)

**ผลทดสอบ:** ไฟล์ 1,000,000 บรรทัดรันผ่านด้วย `-Xmx32m` ใช้ heap ประมาณ 12 MB
memory ขึ้นกับ `batchSize` ไม่ขึ้นกับขนาดไฟล์

| Class | หน้าที่ |
|---|---|
| `StreamingBatchProcessor` | อ่านแบบ streaming และแบ่ง batch |
| `BatchRepository` / `FakeBatchRepository` | ปลายทางจำลอง |
| `MetadataRecord` | model + parse |
| `SampleFileGenerator` | สร้างไฟล์ทดสอบขนาดใหญ่ |

---

## โจทย์ 3: Import ที่รับมือ Connection Error และ Retry

แยก error ตามว่า **retry แล้วมีโอกาสสำเร็จหรือไม่** และ **retry แล้วปลอดภัยหรือไม่**

| สถานการณ์ | Exception | การจัดการ |
|---|---|---|
| เชื่อมต่อไม่ได้ชั่วคราว | `TemporaryConnectionException` | backoff แล้ว retry |
| connection หลุด | `ConnectionLostException` | reconnect + backoff แล้ว retry |
| username/password ผิด | `AuthenticationException` | **ไม่ retry และหยุดทั้ง batch** เพราะรายการที่เหลือจะผิดเหมือนกัน และ retry ซ้ำอาจทำให้ account ถูก lock |
| ข้อมูลเอกสารผิด | `InvalidDocumentException` | ไม่ retry, mark ว่าล้มเหลว แล้วทำรายการถัดไป |
| timeout ไม่รู้ว่าสร้างสำเร็จหรือไม่ | `ImportTimeoutException` | **ห้ามสร้างใหม่ทันที** ต้องค้นด้วย `externalId` ก่อน ถ้าพบแล้วถือว่าสำเร็จ ถ้าไม่พบจึงสร้างใหม่ |

**ป้องกันเอกสารซ้ำ (idempotency)**
เอกสารทุกรายการมี `externalId` ที่ไม่ซ้ำกัน หลัง timeout ระบบจะตั้งค่า `outcomeUnknown = true`
และใน attempt ถัดไปจะเรียก `findByExternalId()` ก่อน `createDocument()` เสมอ
ถ้า timeout จนหมดจำนวน retry และยังยืนยันผลไม่ได้ จะได้สถานะ `UNKNOWN_NEEDS_VERIFICATION` ให้คนตรวจสอบ ไม่ถือว่าล้มเหลว

**Retry policy:** สูงสุด 3 ครั้ง, exponential backoff 200 → 400 ms (มี max delay)
`Sleeper` ถูกแยกออกมาเพื่อให้ test ไม่ต้องรอจริง

**การจำลอง:** `FakeDocumentGateway` กำหนดผลของแต่ละเอกสารล่วงหน้า (ไม่สุ่ม) เพื่อให้รันซ้ำได้ผลเหมือนเดิม
scenario ใน `Main` ครอบคลุมทุกกรณีข้างบน และตรวจว่าเอกสารที่ปลายทางไม่ซ้ำ (`Duplicate documents at target : 0`)

| Class | หน้าที่ |
|---|---|
| `BatchImporter` | logic การ import, retry และแยก error |
| `RetryPolicy` | จำนวนครั้งและ backoff |
| `DocumentGateway` / `FakeDocumentGateway` | ระบบเอกสารปลายทางจำลอง |
| `*Exception` | ลำดับชั้นของ error (`RetryableException` / non-retryable) |
| `ImportStatus`, `ImportResult` | ผลลัพธ์รายรายการ |

## Test

ใช้ test runner แบบง่ายที่เขียนเอง (ไม่ใช้ JUnit ตามข้อจำกัดของโจทย์)

- `StreamingBatchProcessorTest`: batch สุดท้าย, จำนวนพอดี batch, บรรทัดเสีย, ไฟล์ว่าง
- `BatchImporterTest`: retry จนสำเร็จ, retry ครบแล้วหยุด, ข้อมูลผิดไม่ retry, timeout ไม่สร้างซ้ำ, reconnect, auth error หยุด batch, backoff
