package com.vts.hrms.repository;

import com.vts.hrms.entity.RequisitionSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RequisitionSequenceRepository extends JpaRepository<RequisitionSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RequisitionSequence r WHERE r.financialYear = :fy")
    Optional<RequisitionSequence> findByFinancialYearForUpdate(@Param("fy") String fy);
}