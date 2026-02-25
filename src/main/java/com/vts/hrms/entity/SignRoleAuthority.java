package com.vts.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sign_role_authority")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SignRoleAuthority implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sign_role_authority_id", nullable = false)
    private Long signRoleAuthorityId;

    @Column(name = "sign_auth_role_id", nullable = false)
    private Long signAuthRoleId;

    @Column(name = "division_id", nullable = false)
    private Long divisionId;

    @Column(name = "emp_id", nullable = false)
    private Long empId;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_upto")
    private LocalDate validUpto;

    @Column(name = "serial_no", nullable = false)
    private Long serialNo;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Integer isActive;

    @Size(max = 100)
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Size(max = 100)
    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

}
