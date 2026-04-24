package com.vts.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_stamping")
public class AuditStamping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_stamping_id")
    private Long auditStampingId;

    @Column(name = "login_id")
    private Long loginId;

    @Column(name ="user_name")
    private String username;

    @Column(name = "login_date")
    private LocalDate loginDate;

    @Column(name = "login_date_time")
    private LocalDateTime loginDatetime;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "logout_type")
    private String logoutType;

    @Column(name = "logout_date_time")
    private LocalDateTime logoutDateTime;
}
