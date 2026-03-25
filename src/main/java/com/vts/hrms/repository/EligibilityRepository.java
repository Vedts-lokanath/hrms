package com.vts.hrms.repository;

import com.vts.hrms.entity.Eligibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EligibilityRepository extends JpaRepository<Eligibility, Long> {

    List<Eligibility> findAllByIsActive(int isActive);

    List<Eligibility> findAllByIsActiveOrderByEligibilityIdDesc(int i);
}
