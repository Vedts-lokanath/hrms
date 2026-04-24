package com.vts.hrms.repository;

import com.vts.hrms.dto.AuditStampingDTO;
import com.vts.hrms.entity.AuditStamping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AuditStampingRepository extends JpaRepository<AuditStamping, Long> {

    @Query("""
                SELECT NEW com.vts.hrms.dto.AuditStampingDTO(
                    a.auditStampingId, a.loginId, a.username, a.loginDate,
                    a.loginDatetime, a.logoutDateTime, a.ipAddress, a.macAddress, a.logoutType
                )
                FROM AuditStamping a
                WHERE a.loginDate >= :fromDate
                AND a.loginDate < :toDate
                AND a.username = :userName
                ORDER BY a.loginDate DESC
            """)
    List<AuditStampingDTO> auditList(String userName, LocalDate fromDate, LocalDate toDate);


}
