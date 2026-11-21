package com.interview.quiz3;

import com.interview.quiz3.FakeDocumentGateway.Outcome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test แบบไม่ใช้ JUnit (โจทย์กำหนดให้ใช้ Standard Library เท่านั้น)
 * วิธีรัน: java -cp out com.interview.quiz3.BatchImporterTest
 */
public class BatchImporterTest {

    private static int passed;

    public static void main(String[] args) throws InterruptedException {
        temporaryErrorIsRetriedUntilSuccess();
        retryStopsAtMaxAttempts();
        invalidDataIsNotRetried();
        timeoutAfterCreatedDoesNotCreateDuplicate();
        timeoutNotCreatedIsCreatedOnce();
        repeatedTimeoutEndsAsUnknown();
        connectionLostTriggersReconnect();
        authenticationErrorStopsBatch();
        backoffIsExponentialAndCapped();
        System.out.println();
        System.out.println("All " + passed + " checks passed");
    }

    static void temporaryErrorIsRetriedUntilSuccess() throws InterruptedException {
        FakeDocumentGateway gw = new FakeDocumentGateway()
                .script("A", Outcome.TEMPORARY_UNAVAILABLE, Outcome.TEMPORARY_UNAVAILABLE);
        ImportResult r = importer(gw).importOne(doc("A"));
        check(r.getStatus() == ImportStatus.SUCCESS, "should succeed");
        check(r.getAttempts() == 3, "should take 3 attempts");
    }

    static void retryStopsAtMaxAttempts() throws InterruptedException {
        FakeDocumentGateway gw = new FakeDocumentGateway()
                .script("A", Outcome.TEMPORARY_UNAVAILABLE, Outcome.TEMPORARY_UNAVAILABLE,
                        Outcome.TEMPORARY_UNAVAILABLE, Outcome.TEMPORARY_UNAVAILABLE);
        ImportResult r = importer(gw).importOne(doc("A"));
        check(r.getStatus() == ImportStatus.FAILED_RETRY_EXHAUSTED, "should give up");
        check(gw.getCreateCount("A") == 0, "should not be created");
    }

    static void invalidDataIsNotRetried() throws InterruptedException {
        FakeDocumentGateway gw = new FakeDocumentGateway().script("A", Outcome.INVALID_DATA);
        ImportResult r = importer(gw).importOne(doc("A"));
        check(r.getStatus() == ImportStatus.FAILED_INVALID_DATA, "should fail as invalid");
        check(r.getAttempts() == 1, "invalid data must not be retried");
    }

    static void timeoutAfterCreatedDoesNotCreateDuplicate() throws InterruptedException {
        FakeDocumentGateway gw = new FakeDocumentGateway().script("A", Outcome.TIMEOUT_AFTER_CREATED);
        ImportResult r = importer(gw).importOne(doc("A"));
        check(r.getStatus() == ImportStatus.SUCCESS_VERIFIED_AFTER_TIMEOUT, "should be verified success");
        check(gw.getCreateCount("A") == 1, "must be created exactly once");
    }

    static void timeoutNotCreatedIsCreatedOnce() throws InterruptedException {
        FakeDocumentGateway gw = new FakeDocumentGateway().script("A", Outcome.TIMEOUT_NOT_CREATED);
        ImportResult r = importer(gw).importOne(doc("A"));
        check(r.getStatus() == ImportStatus.SUCCESS, "should be created on retry");
        check(gw.getCreateCount("A") == 1, "must be created exactly once");
    }

    static void repeatedTimeoutEndsAsUnknown() throws InterruptedException {
        FakeDocumentGateway gw = new FakeDocumentGateway()
                .script("A", Outcome.TIMEOUT_NOT_CREATED, Outcome.TIMEOUT_NOT_CREATED, Outcome.TIMEOUT_NOT_CREATED);
        ImportResult r = importer(gw).importOne(doc("A"));
        check(r.getStatus() == ImportStatus.UNKNOWN_NEEDS_VERIFICATION, "last timeout -> unknown");
    }

    static void connectionLostTriggersReconnect() throws InterruptedException {
        FakeDocumentGateway gw = new FakeDocumentGateway().script("A", Outcome.CONNECTION_LOST);
        ImportResult r = importer(gw).importOne(doc("A"));
        check(r.getStatus() == ImportStatus.SUCCESS, "should succeed after reconnect");
        check(gw.getConnectCount() == 2, "should connect twice (initial + reconnect)");
    }

    static void authenticationErrorStopsBatch() throws InterruptedException {
        FakeDocumentGateway gw = new FakeDocumentGateway().script("B", Outcome.AUTH_FAILED);
        List<ImportDocument> docs = new ArrayList<>(Arrays.asList(doc("A"), doc("B"), doc("C"), doc("D")));
        BatchImporter.BatchSummary s = importer(gw).importAll(docs);
        check(s.isAborted(), "batch should be aborted");
        check(s.find("A").getStatus() == ImportStatus.SUCCESS, "A done before error");
        check(s.find("B").getStatus() == ImportStatus.FAILED_AUTHENTICATION, "B failed auth");
        check(s.find("B").getAttempts() == 1, "auth error must not be retried");
        check(s.find("C").getStatus() == ImportStatus.NOT_PROCESSED, "C not processed");
        check(s.find("D").getStatus() == ImportStatus.NOT_PROCESSED, "D not processed");
    }

    static void backoffIsExponentialAndCapped() {
        RetryPolicy p = new RetryPolicy(5, 100, 2.0, 500, millis -> { });
        check(p.delayAfterAttempt(1) == 100, "1st delay 100");
        check(p.delayAfterAttempt(2) == 200, "2nd delay 200");
        check(p.delayAfterAttempt(3) == 400, "3rd delay 400");
        check(p.delayAfterAttempt(4) == 500, "capped at 500");
    }

    private static BatchImporter importer(FakeDocumentGateway gw) {
        // ไม่รอจริงใน test
        return new BatchImporter(gw, new RetryPolicy(3, 100, 2.0, 1000, millis -> { }));
    }

    private static ImportDocument doc(String id) {
        return new ImportDocument(id, id + ".pdf");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        passed++;
    }
}
