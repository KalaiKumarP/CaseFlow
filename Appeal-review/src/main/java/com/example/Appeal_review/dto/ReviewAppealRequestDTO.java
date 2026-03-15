package com.example.Appeal_review.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewAppealRequestDTO {

    @NotNull(message = "judgeId is required")
    private Long judgeId;
}