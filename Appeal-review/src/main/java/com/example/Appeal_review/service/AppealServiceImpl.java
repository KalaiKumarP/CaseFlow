package com.example.Appeal_review.service;

import com.example.Appeal_review.client.CaseServiceClient;
import com.example.Appeal_review.client.UserServiceClient;
import com.example.Appeal_review.dto.AppealResponseDTO;
import com.example.Appeal_review.dto.CaseDTO;
import com.example.Appeal_review.dto.ReviewResponseDTO;
import com.example.Appeal_review.dto.UserDTO;
import com.example.Appeal_review.entity.Appeal;
import com.example.Appeal_review.entity.Review;
import com.example.Appeal_review.enums.AppealStatus;
import com.example.Appeal_review.enums.ReviewOutcome;
import com.example.Appeal_review.exception.InvalidAppealStateException;
import com.example.Appeal_review.exception.ResourceNotFoundException;
import com.example.Appeal_review.repository.AppealRepository;
import com.example.Appeal_review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppealServiceImpl implements AppealService {

    private final AppealRepository appealRepository;
    private final ReviewRepository reviewRepository;
    private final CaseServiceClient caseServiceClient;
    private final UserServiceClient userServiceClient;

    // ---------------------------------------------------------------
    // 1. FILE APPEAL
    // ---------------------------------------------------------------
    // Validates that the case is CLOSED before allowing a new appeal.
    // Only a CLOSED case is eligible — open/active cases cannot be appealed.
    // Sets FiledDate to today and Status to SUBMITTED automatically.
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public AppealResponseDTO fileAppeal(Long caseId, String reason) {

        log.info("Filing appeal for caseId={}", caseId);

        // Step 1 — Fetch the case from Module 4.2 via Feign client
        CaseDTO caseDTO = caseServiceClient.getCaseById(caseId);
        if (caseDTO == null) {
            throw new ResourceNotFoundException("Case not found with id: " + caseId);
        }

        // Step 2 — Business rule: only CLOSED cases are eligible for appeal
        if (!"CLOSED".equalsIgnoreCase(caseDTO.getStatus())) {
            throw new InvalidAppealStateException(
                    "Appeal can only be filed for a CLOSED case. " +
                            "Current status: " + caseDTO.getStatus()
            );
        }

        // Step 3 — Prevent duplicate active appeals on the same case
        boolean alreadyPending = appealRepository
                .existsByCaseIdAndStatus(caseId, AppealStatus.SUBMITTED);
        if (alreadyPending) {
            throw new InvalidAppealStateException(
                    "A SUBMITTED appeal already exists for caseId: " + caseId
            );
        }

        // Step 4 — Build and persist the Appeal record
        Appeal appeal = Appeal.builder()
                .caseId(caseId)
                .reason(reason)
                .filedDate(LocalDate.now())
                .status(AppealStatus.SUBMITTED)
                .build();

        Appeal saved = appealRepository.save(appeal);
        log.info("Appeal created with appealId={}", saved.getAppealId());

        return mapToAppealResponse(saved);
    }

    // ---------------------------------------------------------------
    // 2. REVIEW APPEAL
    // ---------------------------------------------------------------
    // Assigns a judge to review the appeal.
    // Appeal must be in SUBMITTED state — already REVIEWED or DECIDED
    // appeals cannot be re-reviewed.
    // Creates a Review record and advances Appeal.Status to REVIEWED.
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public ReviewResponseDTO reviewAppeal(Long appealId, Long judgeId) {

        // userServiceClient is injected via constructor (@RequiredArgsConstructor)

// Inside reviewAppeal() — add after fetching the appeal:
        UserDTO judge = userServiceClient.getUserById(judgeId);
        if (judge == null) {
            throw new ResourceNotFoundException("User not found with id: " + judgeId);
        }
        if (!"JUDGE".equalsIgnoreCase(judge.getRole())) {
            throw new InvalidAppealStateException(
                    "User " + judgeId + " is not a JUDGE. Only judges can review appeals.");
        }

        log.info("Reviewing appealId={} by judgeId={}", appealId, judgeId);

        // Step 1 — Fetch the appeal
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appeal not found with id: " + appealId));

        // Step 2 — Business rule: only SUBMITTED appeals can be reviewed
        if (appeal.getStatus() != AppealStatus.SUBMITTED) {
            throw new InvalidAppealStateException(
                    "Only SUBMITTED appeals can be reviewed. " +
                            "Current status: " + appeal.getStatus()
            );
        }

        // Step 3 — Prevent the same judge from reviewing the same appeal twice
        boolean alreadyReviewed = reviewRepository
                .existsByCaseIdAndJudgeId(appeal.getCaseId(), judgeId);
        if (alreadyReviewed) {
            throw new InvalidAppealStateException(
                    "Judge " + judgeId + " has already reviewed this case."
            );
        }

        // Step 4 — Update Appeal status to REVIEWED
        appeal.setStatus(AppealStatus.REVIEWED);
        appealRepository.save(appeal);

        // Step 5 — Create Review record (outcome is null until decided)
        Review review = Review.builder()
                .caseId(appeal.getCaseId())
                .judgeId(judgeId)
                .reviewDate(LocalDate.now())
                .outcome(null)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Review created with reviewId={}", savedReview.getReviewId());

        return mapToReviewResponse(savedReview, appeal);
    }

    // ---------------------------------------------------------------
    // 3. DECIDE APPEAL
    // ---------------------------------------------------------------
    // Records the final decision on a reviewed appeal.
    // Appeal must be in REVIEWED state before a decision can be made.
    // "Appeal Upheld"    → case is reopened (ACTIVE via Feign call)
    // "Appeal Dismissed" → case remains CLOSED, no further action
    // Updates both Appeal.Status = DECIDED and Review.Outcome.
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public ReviewResponseDTO decideAppeal(Long appealId, String outcome) {

        log.info("Deciding appealId={} with outcome={}", appealId, outcome);

        // Step 1 — Fetch the appeal
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appeal not found with id: " + appealId));

        // Step 2 — Business rule: only REVIEWED appeals can be decided
        if (appeal.getStatus() != AppealStatus.REVIEWED) {
            throw new InvalidAppealStateException(
                    "Only REVIEWED appeals can be decided. " +
                            "Current status: " + appeal.getStatus()
            );
        }

        // Step 3 — Fetch the associated Review record for this case
        List<com.example.Appeal_review.entity.Review> reviews = reviewRepository.findByCaseId(appeal.getCaseId());
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No Review record found for caseId: " + appeal.getCaseId());
        }

        // Take the most recent review for this appeal
        Review review = reviews.get(reviews.size() - 1);

        // Step 4 — Update Review outcome
        ReviewOutcome reviewOutcome;
        try {
            reviewOutcome = ReviewOutcome.valueOf(outcome.replace(" ", "_").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidAppealStateException("Invalid review outcome: " + outcome);
        }
        review.setOutcome(reviewOutcome);
        reviewRepository.save(review);

        // Step 5 — Update Appeal status to DECIDED
        appeal.setStatus(AppealStatus.DECIDED);
        appealRepository.save(appeal);

        // Step 6 — Apply case status change based on outcome
        if (reviewOutcome == ReviewOutcome.APPEAL_UPHELD) {
            log.info("Appeal upheld — updating caseId={} status to ACTIVE",
                    appeal.getCaseId());
            caseServiceClient.updateCaseStatus(appeal.getCaseId(), "ACTIVE");

        } else if (reviewOutcome == ReviewOutcome.APPEAL_DISMISSED) {
            log.info("Appeal dismissed — caseId={} remains CLOSED",
                    appeal.getCaseId());
            // No status change needed; case stays CLOSED

        } else {
            // Unexpected outcome — log a warning but do not reject
            log.warn("Unrecognised outcome '{}' for appealId={}. " +
                    "Case status not modified.", outcome, appealId);
        }

        return mapToReviewResponse(review, appeal);
    }

    // ---------------------------------------------------------------
    // PRIVATE MAPPERS
    // ---------------------------------------------------------------

    private AppealResponseDTO mapToAppealResponse(Appeal appeal) {
        return AppealResponseDTO.builder()
                .appealId(appeal.getAppealId())
                .caseId(appeal.getCaseId())
                .filedDate(appeal.getFiledDate())
                .reason(appeal.getReason())
                .status(appeal.getStatus().name())
                .build();
    }

    private ReviewResponseDTO mapToReviewResponse(Review review, Appeal appeal) {
        return ReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .caseId(review.getCaseId())
                .judgeId(review.getJudgeId())
                .reviewDate(review.getReviewDate())
                .outcome(review.getOutcome() != null ? review.getOutcome().name() : null)
                .appealStatus(appeal.getStatus().name())
                .build();
    }
}