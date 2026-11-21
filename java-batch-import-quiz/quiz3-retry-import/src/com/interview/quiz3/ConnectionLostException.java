package com.interview.quiz3;

/**
 * connection หลุดระหว่างทำงาน -> ต้อง reconnect แล้ว retry
 * (กรณีนี้ถือว่าหลุดก่อนส่ง request สำเร็จ ปลายทางยังไม่ได้สร้างเอกสาร)
 */
public class ConnectionLostException extends RetryableException {

    private static final long serialVersionUID = 1L;

    public ConnectionLostException(String message) {
        super(message);
    }
}
