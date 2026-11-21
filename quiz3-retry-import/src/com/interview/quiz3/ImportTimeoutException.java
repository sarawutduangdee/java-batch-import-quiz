package com.interview.quiz3;

/**
 * timeout หลังส่ง request ไปแล้ว -> ไม่รู้ว่าปลายทางสร้างเอกสารสำเร็จหรือไม่
 * ห้าม retry ทันที ต้องตรวจสอบด้วย externalId ก่อน เพื่อป้องกันเอกสารซ้ำ
 */
public class ImportTimeoutException extends ImportException {

    private static final long serialVersionUID = 1L;

    public ImportTimeoutException(String message) {
        super(message);
    }
}
