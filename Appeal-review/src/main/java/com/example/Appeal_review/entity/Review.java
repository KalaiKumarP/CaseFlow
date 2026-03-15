package com.example.Appeal_review.entity;

import com.example.Appeal_review.enums.ReviewOutcome;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "judge_id", nullable = false)
    private Long judgeId;

    @Column(name = "date", nullable = false)
    private LocalDate reviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 50)
    private ReviewOutcome outcome;
}