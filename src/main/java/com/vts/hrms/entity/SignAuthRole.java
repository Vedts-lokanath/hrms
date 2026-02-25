package com.vts.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sign_auth_role")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SignAuthRole implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sign_auth_role_id", nullable = false)
    private Long signAuthRoleId;

    @NotNull
    @Column(name = "form_module_id", nullable = false)
    private Long formModuleId;

    @Size(max = 12)
    @Column(name = "sign_auth_code", length = 12)
    private String signAuthCode;

    @Size(max = 20)
    @Column(name = "sign_auth_role", length = 20)
    private String signAuthRole;

    @NotNull
    @Column(name = "serial_no", nullable = false)
    private Integer serialNo;

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
