package com.vts.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditStampingDTO implements Serializable {


    private Long auditStampingId;
    private Long loginId;
    private String username;
    private LocalDate loginDate;
    private LocalDateTime loginDatetime;
    private LocalDateTime logoutDateTime;
    private String ipAddress;
    private String macAddress;
    private String logoutType;

}
