package com.example.Appeal_review.service;

import com.example.Appeal_review.dto.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO getReviewById(Long reviewId);

    List<ReviewResponseDTO> getReviewsByCase(Long caseId);

    List<ReviewResponseDTO> getReviewsByJudge(Long judgeId);

    ReviewResponseDTO updateOutcome(Long reviewId, String outcome);
}