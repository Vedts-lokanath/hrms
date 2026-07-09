package com.vts.hrms.repository;

import com.vts.hrms.entity.CashLimit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashLimitRepository extends JpaRepository<CashLimit, Long> {

    List<CashLimit> findAllByOrderByCashLimitIdDesc();

    CashLimit findTopByOrderByCashLimitIdDesc();

    @Modifying
    @Transactional
    @Query("UPDATE CashLimit c SET c.isActive = 0 WHERE c.isActive = 1")
    int deactivateActiveRecords();
}
