package com.vts.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "hrms_handing_over")
public class HandingOver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "handing_over_id")
    private Long handingOverId;

    @Column(name = "from_emp_id")
    private Long fromEmpId;

    @Column(name = "to_emp_id")
    private Long toEmpId;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Size(max = 255)
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Size(max = 255)
    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Integer isActive;
}
