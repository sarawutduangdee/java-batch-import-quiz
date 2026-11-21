package com.interview.quiz3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Import เอกสารทีละรายการ พร้อมจัดการ error แต่ละแบบ
 *
 * | Error                        | การจัดการ                                              |
 * |------------------------------|--------------------------------------------------------|
 * | TemporaryConnectionException | backoff แล้ว retry                                     |
 * | ConnectionLostException      | reconnect + backoff แล้ว retry                         |
 * | ImportTimeoutException       | ตรวจด้วย externalId ก่อน ถ้ามีแล้วถือว่าสำเร็จ ไม่สร้างซ้ำ |
 * | InvalidDocumentException     | ไม่ retry, mark FAILED แล้วทำรายการถัดไป               |
 * | AuthenticationException      | ไม่ retry, หยุดทั้ง batch                               |
 */
public class BatchImporter {

    private final DocumentGateway gateway;
    private final RetryPolicy retryPolicy;

    public BatchImporter(DocumentGateway gateway, RetryPolicy retryPolicy) {
        this.gateway = gateway;
        this.retryPolicy = retryPolicy;
    }

    public BatchSummary importAll(List<ImportDocument> documents) throws InterruptedException {
        List<ImportResult> results = new ArrayList<>();
        boolean aborted = false;

        for (ImportDocument doc : documents) {
            if (aborted) {
                results.add(new ImportResult(doc.getExternalId(), ImportStatus.NOT_PROCESSED, 0,
                        "batch aborted"));
                continue;
            }
            ImportResult result = importOne(doc);
            results.add(result);
            log(result);
            if (result.getStatus() == ImportStatus.FAILED_AUTHENTICATION) {
                aborted = true;
                System.out.println("  !! authentication failed -> stop batch, remaining documents are not processed");
            }
        }
        return new BatchSummary(results, aborted);
    }

    ImportResult importOne(ImportDocument doc) throws InterruptedException {
        String id = doc.getExternalId();
        // true เมื่อเคย timeout -> ไม่รู้ว่าปลายทางสร้างเอกสารแล้วหรือยัง ต้องตรวจก่อนสร้างใหม่
        boolean outcomeUnknown = false;
        String lastError = null;

        for (int attempt = 1; attempt <= retryPolicy.getMaxAttempts(); attempt++) {
            try {
                if (!gateway.isConnected()) {
                    gateway.connect();
                }
                if (outcomeUnknown) {
                    Optional<String> existing = gateway.findByExternalId(id);
                    if (existing.isPresent()) {
                        return new ImportResult(id, ImportStatus.SUCCESS_VERIFIED_AFTER_TIMEOUT, attempt,
                                "found existing document " + existing.get() + ", not created again");
                    }
                    outcomeUnknown = false; // ยืนยันแล้วว่ายังไม่มี -> สร้างได้อย่างปลอดภัย
                }
                String remoteId = gateway.createDocument(doc);
                return new ImportResult(id, ImportStatus.SUCCESS, attempt, "created " + remoteId);

            } catch (InvalidDocumentException e) {
                return new ImportResult(id, ImportStatus.FAILED_INVALID_DATA, attempt, e.getMessage());

            } catch (AuthenticationException e) {
                return new ImportResult(id, ImportStatus.FAILED_AUTHENTICATION, attempt, e.getMessage());

            } catch (ImportTimeoutException e) {
                outcomeUnknown = true;
                lastError = e.getMessage();
                retryLog(id, attempt, "TIMEOUT", e.getMessage() + " -> will check by externalId before retry");

            } catch (RetryableException e) {
                lastError = e.getMessage();
                retryLog(id, attempt, e.getClass().getSimpleName(), e.getMessage());

            } catch (ImportException e) {
                // error ชนิดอื่นที่ไม่รู้จัก -> ไม่ retry เพื่อความปลอดภัย
                return new ImportResult(id, ImportStatus.FAILED_INVALID_DATA, attempt, "unexpected: " + e.getMessage());
            }

            if (attempt < retryPolicy.getMaxAttempts()) {
                retryPolicy.backoff(attempt);
            }
        }

        if (outcomeUnknown) {
            return new ImportResult(id, ImportStatus.UNKNOWN_NEEDS_VERIFICATION, retryPolicy.getMaxAttempts(),
                    "last error was timeout and result could not be verified: " + lastError);
        }
        return new ImportResult(id, ImportStatus.FAILED_RETRY_EXHAUSTED, retryPolicy.getMaxAttempts(),
                "gave up after " + retryPolicy.getMaxAttempts() + " attempts: " + lastError);
    }

    private void retryLog(String id, int attempt, String type, String message) {
        String next = attempt < retryPolicy.getMaxAttempts()
                ? "retry in " + retryPolicy.delayAfterAttempt(attempt) + " ms"
                : "no attempts left";
        System.out.printf("  %s attempt %d/%d failed: %s (%s) -> %s%n",
                id, attempt, retryPolicy.getMaxAttempts(), type, message, next);
    }

    private void log(ImportResult result) {
        System.out.println("=> " + result);
    }

    public static class BatchSummary {
        private final List<ImportResult> results;
        private final boolean aborted;

        BatchSummary(List<ImportResult> results, boolean aborted) {
            this.results = results;
            this.aborted = aborted;
        }

        public List<ImportResult> getResults() {
            return results;
        }

        public boolean isAborted() {
            return aborted;
        }

        public long count(ImportStatus status) {
            long n = 0;
            for (ImportResult r : results) {
                if (r.getStatus() == status) {
                    n++;
                }
            }
            return n;
        }

        public ImportResult find(String externalId) {
            for (ImportResult r : results) {
                if (r.getExternalId().equals(externalId)) {
                    return r;
                }
            }
            return null;
        }
    }
}
