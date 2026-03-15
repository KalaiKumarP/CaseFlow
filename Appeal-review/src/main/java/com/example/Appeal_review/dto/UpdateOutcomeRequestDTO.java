package com.example.Appeal_review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateOutcomeRequestDTO {

    @NotBlank(message = "outcome is required")
    private String outcome;
}