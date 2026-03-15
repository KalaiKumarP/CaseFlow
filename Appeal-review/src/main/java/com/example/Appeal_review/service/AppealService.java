package com.example.Appeal_review.service;

import com.example.Appeal_review.dto.AppealResponseDTO;
import com.example.Appeal_review.dto.ReviewResponseDTO;

public interface AppealService {

    AppealResponseDTO fileAppeal(Long caseId, String reason);

    ReviewResponseDTO reviewAppeal(Long appealId, Long judgeId);

    ReviewResponseDTO decideAppeal(Long appealId, String outcome);
}