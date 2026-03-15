package com.example.Appeal_review.client;

import com.example.Appeal_review.dto.CaseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//@FeignClient(name = "case-service", url = "${case.service.url}")
public interface CaseServiceClient {

    @GetMapping("/api/cases/{caseId}")
    CaseDTO getCaseById(@PathVariable Long caseId);

    @PatchMapping("/api/cases/{caseId}/status")
    void updateCaseStatus(
            @PathVariable Long caseId,
            @RequestParam String status
    );
}