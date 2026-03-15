package com.example.Appeal_review.repository;

import com.example.Appeal_review.entity.Review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // All reviews conducted for a specific case
    List<Review> findByCaseId(Long caseId);

    // All reviews assigned to a specific judge
    List<Review> findByJudgeId(Long judgeId);

    // Check if a case has been reviewed by a particular judge already
    boolean existsByCaseIdAndJudgeId(Long caseId, Long judgeId);
}