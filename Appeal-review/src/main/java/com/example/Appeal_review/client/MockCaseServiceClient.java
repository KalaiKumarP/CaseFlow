package com.example.Appeal_review.client;

import com.example.Appeal_review.dto.CaseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
// Remove @Profile("mock") if you want this active always during standalone testing
// Add it back and use @Profile("prod") on real client when integrating
public class MockCaseServiceClient implements CaseServiceClient {

    // ----------------------------------------------------------------
    // Hardcoded test cases — add/edit freely to test different scenarios
    // ----------------------------------------------------------------
    // caseId 101 → CLOSED   (valid for filing appeal)
    // caseId 102 → CLOSED   (another valid case)
    // caseId 103 → ACTIVE   (should trigger 409 when filing)
    // caseId 104 → PENDING  (should trigger 409 when filing)
    // ----------------------------------------------------------------

    @Override
    public CaseDTO getCaseById(Long caseId) {
        log.warn("[MOCK] CaseServiceClient.getCaseById called — caseId={}", caseId);

        return switch (caseId.intValue()) {
            case 101 -> new CaseDTO(101L, "State vs. Ramesh Kumar", "CLOSED");
            case 102 -> new CaseDTO(102L, "Priya vs. City Council", "CLOSED");
            case 103 -> new CaseDTO(103L, "Tax Dispute - ABC Corp", "ACTIVE");
            case 104 -> new CaseDTO(104L, "Property Dispute - Block 5", "PENDING");
            default  -> null; // triggers ResourceNotFoundException in service
        };
    }

    @Override
    public void updateCaseStatus(Long caseId, String status) {
        log.warn("[MOCK] CaseServiceClient.updateCaseStatus called — " +
                "caseId={}, newStatus={}", caseId, status);
        // No real call — just logs the action
        // When Module 4.2 is ready, delete this file and uncomment
        // case.service.url in application.properties
    }
}