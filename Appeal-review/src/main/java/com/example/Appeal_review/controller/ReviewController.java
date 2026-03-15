package com.example.Appeal_review.controller;

import com.example.Appeal_review.dto.ApiResponse;
import com.example.Appeal_review.dto.ReviewResponseDTO;
import com.example.Appeal_review.dto.UpdateOutcomeRequestDTO;
import com.example.Appeal_review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    // ------------------------------------------------------------------
    // GET /reviews/{reviewId}
    // Fetch a single review by its ID.
    // Used by judges and clerks to view a specific review record.
    // ------------------------------------------------------------------
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReviewById(
            @PathVariable Long reviewId) {

        log.info("GET /reviews/{}", reviewId);

        ReviewResponseDTO response = reviewService.getReviewById(reviewId);

        return ResponseEntity.ok(
                ApiResponse.success("Review fetched successfully.", response));
    }

    // ------------------------------------------------------------------
    // GET /reviews/case/{caseId}
    // Fetch all reviews for a given case.
    // Used to display the full review history of a case.
    // ------------------------------------------------------------------
    @GetMapping("/case/{caseId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByCase(
            @PathVariable Long caseId) {

        log.info("GET /reviews/case/{}", caseId);

        List<ReviewResponseDTO> response = reviewService.getReviewsByCase(caseId);

        return ResponseEntity.ok(
                ApiResponse.success("Reviews fetched successfully.", response));
    }

    // ------------------------------------------------------------------
    // GET /reviews/judge/{judgeId}
    // Fetch all reviews assigned to a specific judge.
    // Powers the Judge Dashboard — shows all cases under review.
    // ------------------------------------------------------------------
    @GetMapping("/judge/{judgeId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByJudge(
            @PathVariable Long judgeId) {

        log.info("GET /reviews/judge/{}", judgeId);

        List<ReviewResponseDTO> response = reviewService.getReviewsByJudge(judgeId);

        return ResponseEntity.ok(
                ApiResponse.success("Reviews fetched successfully.", response));
    }

    // ------------------------------------------------------------------
    // PATCH /reviews/{reviewId}/outcome
    // Update the outcome of a review before the appeal is decided.
    // Reuses DecideAppealRequestDTO since it only needs an outcome string.
    // ------------------------------------------------------------------
    // ReviewController.java — PATCH endpoint updated
    @PatchMapping("/{reviewId}/outcome")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> updateOutcome(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateOutcomeRequestDTO request) {

        ReviewResponseDTO response = reviewService.updateOutcome(
                reviewId, request.getOutcome());

        return ResponseEntity.ok(
                ApiResponse.success("Review outcome updated.", response));
    }
}