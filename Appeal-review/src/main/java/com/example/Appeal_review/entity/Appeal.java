package com.example.Appeal_review.entity;
import com.example.Appeal_review.enums.AppealStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "appeal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appeal_id")
    private Long appealId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "filed_date", nullable = false)
    private LocalDate filedDate;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppealStatus status;
}