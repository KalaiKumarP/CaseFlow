package com.example.Appeal_review.repository;

import com.example.Appeal_review.entity.Appeal;
import com.example.Appeal_review.enums.AppealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppealRepository extends JpaRepository<Appeal, Long> {

    // Fetch all appeals filed for a specific case
    List<Appeal> findByCaseId(Long caseId);

    // Filter appeals by status (e.g. all SUBMITTED appeals pending review)
    List<Appeal> findByStatus(AppealStatus status);

    // Check whether a case already has an appeal in a given status
    boolean existsByCaseIdAndStatus(Long caseId, AppealStatus status);
}