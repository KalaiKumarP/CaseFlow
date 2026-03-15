package com.example.Appeal_review.dto;


import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewResponseDTO {
    private Long reviewId;
    private Long caseId;
    private Long judgeId;
    private LocalDate reviewDate;
    private String outcome;
    private String appealStatus;
}