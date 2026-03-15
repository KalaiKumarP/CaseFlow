package com.example.Appeal_review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileAppealRequestDTO {

    @NotNull(message = "caseId is required")
    private Long caseId;

    @NotBlank(message = "reason is required")
    private String reason;
}