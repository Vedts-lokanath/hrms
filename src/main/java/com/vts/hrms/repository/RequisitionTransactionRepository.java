package com.vts.hrms.repository;

import com.vts.hrms.entity.RequisitionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequisitionTransactionRepository extends JpaRepository<RequisitionTransaction, Long> {

    List<RequisitionTransaction> findAllByRequisitionIdAndIsActive(Long requisitionId, int isActive);

    List<RequisitionTransaction> findByRequisitionIdAndIsActiveOrderByActionDateDesc(Long requisitionId, int isActive);

    List<RequisitionTransaction> findAllByActionToAndStatusCodeInAndIsActive(Long empId, List<String> statusCodes, int i);

    List<RequisitionTransaction> findAllByIsActive(int isActive);
}
