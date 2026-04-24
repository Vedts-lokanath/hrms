package com.vts.hrms.repository;

import com.vts.hrms.entity.Sponsorship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SponsorshipRepository extends JpaRepository<Sponsorship, Long> {

    @Query("""
            SELECT a FROM Sponsorship a WHERE a.degreeType = :type AND a.isActive=1
            ORDER BY a.sponsorshipId DESC
            """)
    List<Sponsorship> findAllByDegreeType(String type);
}
