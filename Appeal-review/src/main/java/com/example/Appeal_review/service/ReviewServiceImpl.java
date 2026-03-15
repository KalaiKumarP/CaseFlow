package com.example.Appeal_review.service;
import com.example.Appeal_review.enums.ReviewOutcome;
import com.example.Appeal_review.dto.ReviewResponseDTO;
import com.example.Appeal_review.entity.Review;
import com.example.Appeal_review.exception.ResourceNotFoundException;
import com.example.Appeal_review.repository.AppealRepository;
import com.example.Appeal_review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppealRepository appealRepository;

    // ------------------------------------------------------------------
    // GET REVIEW BY ID
    // Fetches a single review record by its primary key.
    // Throws 404 if not found.
    // ------------------------------------------------------------------
    @Override
    public ReviewResponseDTO getReviewById(Long reviewId) {

        log.info("Fetching review for reviewId={}", reviewId);

        com.example.Appeal_review.entity.Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id: " + reviewId));

        return mapToResponse(review);
    }

    // ------------------------------------------------------------------
    // GET REVIEWS BY CASE
    // Returns all review records associated with a given caseId.
    // Useful for tracking the full review history of a case.
    // ------------------------------------------------------------------
    @Override
    public List<ReviewResponseDTO> getReviewsByCase(Long caseId) {

        log.info("Fetching reviews for caseId={}", caseId);

        return reviewRepository.findByCaseId(caseId)
                .stream()
                .map((com.example.Appeal_review.entity.Review review) -> mapToResponse(review))
                .toList();
    }

    // ------------------------------------------------------------------
    // GET REVIEWS BY JUDGE
    // Returns all reviews conducted by a specific judge.
    // Useful for the Judge Dashboard in the UI.
    // ------------------------------------------------------------------
    @Override
    public List<ReviewResponseDTO> getReviewsByJudge(Long judgeId) {

        log.info("Fetching reviews for judgeId={}", judgeId);

        return reviewRepository.findByJudgeId(judgeId)
                .stream()
                .map((com.example.Appeal_review.entity.Review review) -> mapToResponse(review))
                .toList();
    }

    // ------------------------------------------------------------------
    // UPDATE OUTCOME
    // Allows a judge to update the outcome of an existing review.
    // Business rule: the linked appeal must be in REVIEWED state —
    // outcome cannot be changed after appeal is DECIDED.
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public ReviewResponseDTO updateOutcome(Long reviewId, String outcome) {

        log.info("Updating outcome for reviewId={}, outcome={}", reviewId, outcome);

        // Step 1 — Fetch the review
        com.example.Appeal_review.entity.Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id: " + reviewId));

        // Step 2 — Fetch the related appeal via caseId
        List<com.example.Appeal_review.entity.Appeal> appeals = appealRepository.findByCaseId(review.getCaseId());
        if (appeals.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No appeal found for caseId: " + review.getCaseId());
        }

        // Take the most recent appeal for this case
        com.example.Appeal_review.entity.Appeal appeal = appeals.get(appeals.size() - 1);

        // Step 3 — Business rule: outcome can only be updated if appeal
        // is still in REVIEWED state (not yet DECIDED)
        if (appeal.getStatus() == com.example.Appeal_review.enums.AppealStatus.DECIDED) {
            throw new IllegalStateException(
                    "Cannot update outcome — appeal is already DECIDED.");
        }

        if (appeal.getStatus() == com.example.Appeal_review.enums.AppealStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Cannot update outcome — appeal has not been reviewed yet.");
        }

        // Step 4 — Update and save
        ReviewOutcome reviewOutcome;
        try {
            reviewOutcome = ReviewOutcome.valueOf(outcome.replace(" ", "_").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid review outcome: " + outcome);
        }
        review.setOutcome(reviewOutcome);
        com.example.Appeal_review.entity.Review saved = reviewRepository.save(review);
        log.info("Outcome updated for reviewId={}", reviewId);

        return mapToResponse(saved);
    }

    // ------------------------------------------------------------------
    // PRIVATE MAPPER
    // ------------------------------------------------------------------
    private ReviewResponseDTO mapToResponse(Review review) {
        return ReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .caseId(review.getCaseId())
                .judgeId(review.getJudgeId())
                .reviewDate(review.getReviewDate())
                .outcome(review.getOutcome() != null ? review.getOutcome().name() : null)
                .appealStatus(null)   // not available without appeal lookup here
                .build();
    }
}