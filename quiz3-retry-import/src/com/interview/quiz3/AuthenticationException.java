package com.interview.quiz3;

/**
 * username/password ไม่ถูกต้อง -> ห้าม retry และต้องหยุดทั้ง batch
 * เพราะทุกรายการที่เหลือจะล้มเหลวเหมือนกัน และ retry ซ้ำอาจทำให้ account ถูก lock
 */
public class AuthenticationException extends ImportException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
