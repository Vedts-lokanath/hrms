package com.vts.hrms.repository;

import com.vts.hrms.dto.EvaluationDTO;
import com.vts.hrms.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {


    @Query("""
                SELECT new com.vts.hrms.dto.EvaluationDTO(
                    e.evaluationId,
                    e.requisitionId,
                    e.traineeId,
                    p.programId,
                    e.impact,
                    p.programName,
                    r.fromDate,
                    r.toDate
                )
                FROM Evaluation e
                LEFT JOIN Requisition r ON r.requisitionId = e.requisitionId
                LEFT JOIN Program p ON p.programId = r.programId
                WHERE e.isActive = 1
            """)
    List<EvaluationDTO> findEvaluationData();

    @Query("""
                SELECT new com.vts.hrms.dto.EvaluationDTO(
                    e.evaluationId,
                    e.requisitionId,
                    e.traineeId,
                    p.programId,
                    e.impact,
                    p.programName,
                    r.fromDate,
                    r.toDate
                )
                FROM Evaluation e
                LEFT JOIN Requisition r ON r.requisitionId = e.requisitionId
                LEFT JOIN Program p ON p.programId = r.programId
                WHERE e.isActive = 1 AND e.traineeId = :id
            """)
    List<EvaluationDTO> findByEmployee(@Param("id") Long id);
}
