package com.interview.quiz3;

/**
 * ข้อผิดพลาดทั้งหมดที่เกิดจากระบบเอกสารปลายทาง
 */
public abstract class ImportException extends Exception {

    private static final long serialVersionUID = 1L;

    protected ImportException(String message) {
        super(message);
    }
}
