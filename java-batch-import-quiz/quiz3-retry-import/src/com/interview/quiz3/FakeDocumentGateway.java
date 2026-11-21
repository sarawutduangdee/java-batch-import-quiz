package com.interview.quiz3;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ระบบเอกสารจำลอง ไม่เชื่อม FileNet จริง
 *
 * กำหนดผลลัพธ์ของแต่ละเอกสารล่วงหน้าด้วย {@link #script(String, Outcome...)}
 * ทุกครั้งที่เรียก createDocument จะดึงผลลัพธ์ตัวถัดไปมาใช้ ถ้าหมดแล้วจะสร้างสำเร็จ
 * (ใช้ scenario แบบกำหนดตายตัวแทนการสุ่ม เพื่อให้ผลรันซ้ำได้เหมือนเดิมและทดสอบได้)
 */
public class FakeDocumentGateway implements DocumentGateway {

    public enum Outcome {
        OK,
        TEMPORARY_UNAVAILABLE,
        CONNECTION_LOST,
        AUTH_FAILED,
        INVALID_DATA,
        /** ปลายทางสร้างเอกสารสำเร็จ แต่ response กลับมาไม่ทัน */
        TIMEOUT_AFTER_CREATED,
        /** timeout ก่อนปลายทางจะสร้างเอกสาร */
        TIMEOUT_NOT_CREATED
    }

    private final Map<String, Deque<Outcome>> scenarios = new HashMap<>();
    /** externalId -> remote id (ข้อมูลที่ "อยู่" ในระบบปลายทาง) */
    private final Map<String, String> store = new HashMap<>();
    /** จำนวนครั้งที่สร้างเอกสารของแต่ละ externalId ใช้ตรวจว่ามีเอกสารซ้ำหรือไม่ */
    private final Map<String, Integer> createCount = new HashMap<>();

    private boolean connected;
    private int connectCount;
    private int sequence;

    public FakeDocumentGateway script(String externalId, Outcome... outcomes) {
        scenarios.put(externalId, new ArrayDeque<>(Arrays.asList(outcomes)));
        return this;
    }

    @Override
    public void connect() {
        connected = true;
        connectCount++;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String createDocument(ImportDocument document) throws ImportException {
        if (!connected) {
            throw new ConnectionLostException("not connected");
        }
        String id = document.getExternalId();
        Deque<Outcome> queue = scenarios.get(id);
        Outcome outcome = (queue == null || queue.isEmpty()) ? Outcome.OK : queue.poll();

        switch (outcome) {
            case TEMPORARY_UNAVAILABLE:
                throw new TemporaryConnectionException("service temporarily unavailable");
            case CONNECTION_LOST:
                connected = false;
                throw new ConnectionLostException("connection reset");
            case AUTH_FAILED:
                connected = false;
                throw new AuthenticationException("invalid username or password");
            case INVALID_DATA:
                throw new InvalidDocumentException("required property 'documentType' is missing");
            case TIMEOUT_AFTER_CREATED:
                save(id);
                throw new ImportTimeoutException("read timed out after 30s");
            case TIMEOUT_NOT_CREATED:
                throw new ImportTimeoutException("read timed out after 30s");
            case OK:
            default:
                return save(id);
        }
    }

    @Override
    public Optional<String> findByExternalId(String externalId) throws ImportException {
        if (!connected) {
            throw new ConnectionLostException("not connected");
        }
        return Optional.ofNullable(store.get(externalId));
    }

    private String save(String externalId) {
        String remoteId = "{FN-" + String.format("%04d", ++sequence) + "}";
        store.put(externalId, remoteId);
        Integer count = createCount.get(externalId);
        createCount.put(externalId, count == null ? 1 : count + 1);
        return remoteId;
    }

    public int getCreateCount(String externalId) {
        Integer count = createCount.get(externalId);
        return count == null ? 0 : count;
    }

    /** จำนวน externalId ที่ถูกสร้างมากกว่า 1 ครั้ง (ควรเป็น 0) */
    public int countDuplicates() {
        int duplicates = 0;
        for (int count : createCount.values()) {
            if (count > 1) {
                duplicates++;
            }
        }
        return duplicates;
    }

    public int getConnectCount() {
        return connectCount;
    }
}
