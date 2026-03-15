package com.example.Appeal_review.controller;

import com.example.Appeal_review.dto.*;
import com.example.Appeal_review.repository.AppealRepository;
import com.example.Appeal_review.service.AppealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appeals")
@RequiredArgsConstructor
@Slf4j
public class AppealController {

    private final AppealService appealService;
    private final AppealRepository appealRepository;

    // ------------------------------------------------------------------
    // POST /appeals/file
    // Files a new appeal against a CLOSED case.
    // Returns 201 CREATED with the new Appeal record.
    // ------------------------------------------------------------------
    @PostMapping("/file")
    public ResponseEntity<ApiResponse<AppealResponseDTO>> fileAppeal(
            @Valid @RequestBody FileAppealRequestDTO request) {

        log.info("POST /appeals/file — caseId={}", request.getCaseId());

        AppealResponseDTO response = appealService.fileAppeal(
                request.getCaseId(),
                request.getReason()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appeal filed successfully.", response));
    }

    // ------------------------------------------------------------------
    // PUT /appeals/{appealId}/review
    // Assigns a judge and moves appeal status to REVIEWED.
    // Creates the Review record.
    // Returns 200 OK with the Review details.
    // ------------------------------------------------------------------
    @PutMapping("/{appealId}/review")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> reviewAppeal(
            @PathVariable Long appealId,
            @Valid @RequestBody ReviewAppealRequestDTO request) {

        log.info("PUT /appeals/{}/review — judgeId={}", appealId, request.getJudgeId());

        ReviewResponseDTO response = appealService.reviewAppeal(
                appealId,
                request.getJudgeId()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Appeal marked as reviewed.", response));
    }

    // ------------------------------------------------------------------
    // PUT /appeals/{appealId}/decision
    // Records the final outcome of a reviewed appeal.
    // If upheld → triggers case status update to ACTIVE via Feign.
    // Returns 200 OK with the updated Review + Appeal status.
    // ------------------------------------------------------------------
    @PutMapping("/{appealId}/decision")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> decideAppeal(
            @PathVariable Long appealId,
            @Valid @RequestBody DecideAppealRequestDTO request) {

        log.info("PUT /appeals/{}/decision — outcome={}", appealId, request.getOutcome());

        ReviewResponseDTO response = appealService.decideAppeal(
                appealId,
                request.getOutcome()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Appeal decision recorded.", response));
    }

    // ------------------------------------------------------------------
    // GET /appeals/case/{caseId}
    // Retrieves all appeals filed for a given case.
    // Useful for lawyers and judges to check appeal history.
    // Returns 200 OK with a list (empty list if none exist).
    // ------------------------------------------------------------------
    @GetMapping("/case/{caseId}")
    public ResponseEntity<ApiResponse<List<AppealResponseDTO>>> getAppealsByCase(
            @PathVariable Long caseId) {

        log.info("GET /appeals/case/{}", caseId);

        List<AppealResponseDTO> appeals = appealRepository
                .findByCaseId(caseId)
                .stream()
                .map(appeal -> AppealResponseDTO.builder()
                        .appealId(appeal.getAppealId())
                        .caseId(appeal.getCaseId())
                        .filedDate(appeal.getFiledDate())
                        .reason(appeal.getReason())
                        .status(appeal.getStatus().name())
                        .build())
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Appeals fetched successfully.", appeals));
    }
}
/*

        ---

        ## API Flow — Step by Step
```
Step 1 ─ Litigant / Lawyer files an appeal
POST /appeals/file
Body: { caseId, reason }
        │
        ▼
Service checks Case.Status == CLOSED   ──✗──▶ 409 Conflict
              │ ✓
Saves Appeal { status: SUBMITTED }
        │
        ▼
Returns 201 Created

Step 2 ─ Court Clerk / Admin assigns a Judge
PUT /appeals/{appealId}/review
Body: { judgeId }
        │
        ▼
Service checks Appeal.Status == SUBMITTED  ──✗──▶ 409 Conflict
              │ ✓
Appeal.Status → REVIEWED
Creates Review { outcome: null }
        │
        ▼
Returns 200 OK

Step 3 ─ Judge records the decision
PUT /appeals/{appealId}/decision
Body: { outcome: "Appeal Upheld" | "Appeal Dismissed" }
        │
        ▼
Service checks Appeal.Status == REVIEWED  ──✗──▶ 409 Conflict
              │ ✓
Review.Outcome  → set
Appeal.Status   → DECIDED
              │
                      ├─ "Appeal Upheld"    → Feign: PATCH Case.Status = ACTIVE
              └─ "Appeal Dismissed" → Case stays CLOSED
              │
                      ▼
Returns 200 OK

Step 4 ─ Anyone checks the appeal history for a case
GET /appeals/case/{caseId}
        │
        ▼
Returns list of all Appeals for that case*/