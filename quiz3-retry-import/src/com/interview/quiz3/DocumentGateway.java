package com.interview.quiz3;

import java.util.Optional;

/**
 * การเชื่อมต่อระบบเอกสารปลายทาง (ในระบบจริงคือ FileNet / ECM)
 */
public interface DocumentGateway {

    void connect() throws ImportException;

    boolean isConnected();

    /**
     * สร้างเอกสารที่ปลายทาง
     *
     * @return id ของเอกสารที่ปลายทาง
     */
    String createDocument(ImportDocument document) throws ImportException;

    /**
     * ค้นหาเอกสารที่ปลายทางด้วย externalId ใช้หลัง timeout เพื่อตรวจว่าเอกสารถูกสร้างไปแล้วหรือยัง
     */
    Optional<String> findByExternalId(String externalId) throws ImportException;
}
