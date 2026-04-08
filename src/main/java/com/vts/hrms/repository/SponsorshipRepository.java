package com.vts.hrms.repository;
import com.vts.hrms.entity.Sponsorship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SponsorshipRepository  extends JpaRepository<Sponsorship, Long> {

    List<Sponsorship> findAllByIsActive(Integer isActive);

}
