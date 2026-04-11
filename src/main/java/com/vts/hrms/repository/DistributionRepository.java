package com.vts.hrms.repository;
import com.vts.hrms.entity.Distribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistributionRepository extends JpaRepository<Distribution, Long> {

    List<Distribution> findAllByIsActive(int isActive);
}
