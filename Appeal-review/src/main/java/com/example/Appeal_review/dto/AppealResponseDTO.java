package com.example.Appeal_review.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppealResponseDTO {
    private Long appealId;
    private Long caseId;
    private LocalDate filedDate;
    private String reason;
    private String status;
}