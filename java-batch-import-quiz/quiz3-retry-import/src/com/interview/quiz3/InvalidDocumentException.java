package com.interview.quiz3;

/**
 * ข้อมูลเอกสารไม่ถูกต้อง -> ไม่ retry (ส่งกี่ครั้งก็ผิดเหมือนเดิม) ข้ามไปรายการถัดไป
 */
public class InvalidDocumentException extends ImportException {

    private static final long serialVersionUID = 1L;

    public InvalidDocumentException(String message) {
        super(message);
    }
}
