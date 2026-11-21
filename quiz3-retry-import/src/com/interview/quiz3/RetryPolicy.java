package com.interview.quiz3;

/**
 * กำหนดจำนวนครั้งสูงสุดและเวลารอแบบ exponential backoff
 * เช่น initialDelay=200ms, multiplier=2 -> รอ 200, 400, 800 ms ...
 */
public class RetryPolicy {

    /** แยกการรอออกมา เพื่อให้ test ไม่ต้องรอจริง */
    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private final int maxAttempts;
    private final long initialDelayMillis;
    private final double multiplier;
    private final long maxDelayMillis;
    private final Sleeper sleeper;

    public RetryPolicy(int maxAttempts, long initialDelayMillis, double multiplier,
                       long maxDelayMillis, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.initialDelayMillis = initialDelayMillis;
        this.multiplier = multiplier;
        this.maxDelayMillis = maxDelayMillis;
        this.sleeper = sleeper;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    /** เวลารอก่อน attempt ถัดไป (attempt เริ่มที่ 1) */
    public long delayAfterAttempt(int attempt) {
        double delay = initialDelayMillis * Math.pow(multiplier, attempt - 1);
        return (long) Math.min(delay, maxDelayMillis);
    }

    public void backoff(int attempt) throws InterruptedException {
        sleeper.sleep(delayAfterAttempt(attempt));
    }
}
