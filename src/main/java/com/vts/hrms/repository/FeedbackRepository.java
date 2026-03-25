package com.vts.hrms.repository;

import com.vts.hrms.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findAllByParticipantIdAndIsActiveOrderByFeedbackIdDesc(Long empId, int isActive);

    List<Feedback> findByIsActiveOrderByFeedbackIdDesc(int isActive);

    List<Feedback> findAllByParticipantIdInAndIsActiveOrderByFeedbackIdDesc(List<Long> empIds, int isActive);
}
