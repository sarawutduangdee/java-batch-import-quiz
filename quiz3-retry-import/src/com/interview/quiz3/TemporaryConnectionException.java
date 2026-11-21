package com.interview.quiz3;

/**
 * เชื่อมต่อไม่ได้ชั่วคราว (เช่น ปลายทาง busy / 503) -> retry ได้
 */
public class TemporaryConnectionException extends RetryableException {

    private static final long serialVersionUID = 1L;

    public TemporaryConnectionException(String message) {
        super(message);
    }
}
