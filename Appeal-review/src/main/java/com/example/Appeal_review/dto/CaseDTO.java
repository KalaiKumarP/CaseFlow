package com.example.Appeal_review.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CaseDTO {
    private Long caseId;
    private String title;
    private String status;   // ACTIVE | CLOSED | PENDING etc.
}