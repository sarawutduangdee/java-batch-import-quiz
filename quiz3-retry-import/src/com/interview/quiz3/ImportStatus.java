package com.interview.quiz3;

public enum ImportStatus {
    /** สร้างเอกสารสำเร็จ */
    SUCCESS,
    /** timeout แต่ตรวจสอบแล้วพบว่าปลายทางสร้างเอกสารสำเร็จ (ไม่สร้างซ้ำ) */
    SUCCESS_VERIFIED_AFTER_TIMEOUT,
    /** ข้อมูลเอกสารไม่ถูกต้อง */
    FAILED_INVALID_DATA,
    /** error ชั่วคราวจน retry ครบจำนวนครั้ง */
    FAILED_RETRY_EXHAUSTED,
    /** timeout แล้วตรวจสอบผลไม่ได้จน retry ครบ ต้องให้คนตรวจสอบ */
    UNKNOWN_NEEDS_VERIFICATION,
    /** username/password ไม่ถูกต้อง (รายการที่ทำให้ batch หยุด) */
    FAILED_AUTHENTICATION,
    /** ไม่ได้ประมวลผลเพราะ batch ถูกหยุดก่อน */
    NOT_PROCESSED
}
