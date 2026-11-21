package com.interview.quiz3;

/**
 * error ชั่วคราว ลองใหม่แล้วมีโอกาสสำเร็จ
 */
public abstract class RetryableException extends ImportException {

    private static final long serialVersionUID = 1L;

    protected RetryableException(String message) {
        super(message);
    }
}
