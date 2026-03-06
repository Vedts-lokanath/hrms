package com.vts.hrms.repository;

import com.vts.hrms.entity.Requisition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequisitionRepository extends JpaRepository<Requisition, Long> {

    List<Requisition> findAllByIsActive(int isActive);

    List<Requisition> findAllByStatusAndIsActive(String status, int isActive);

    List<Requisition> findAllByStatusInAndIsActive(List<String> statusCodes, int isActive);

    @Query("""
                SELECT DISTINCT r
                FROM Requisition r
                JOIN RequisitionTransaction t
                    ON r.requisitionId = t.requisitionId
                WHERE t.actionTo = :empId
                  AND r.status=t.statusCode
                  AND t.statusCode IN :statusCodes
                  AND t.isActive = 1
                  AND r.isActive = 1
            """)
    List<Requisition> findApprovalList(
            @Param("empId") Long empId,
            @Param("statusCodes") List<String> statusCodes
    );

    List<Requisition> findAllByIsActiveOrderByRequisitionIdDesc(int isActive);
}
