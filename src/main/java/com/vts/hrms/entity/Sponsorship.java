package com.vts.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "hrms_sponsorship")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Sponsorship implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sponsorship_id")
    private Long sponsorshipId;

    @Column(name = "emp_id")
    private Long empId;

    @Size(max = 5)
    @Column(name = "degree_type", length = 5)
    private String degreeType;

    @Column(name = "delegated_power")
    private String delegatedPower;

    @Column(name = "discipline")
    private String discipline;

    @Column(name = "subject")
    private String subject;

    @Column(name = "university")
    private String university;

    @Column(name = "preference")
    private String preference;

    @Column(name = "city")
    private String city;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;


    @Column(name = "period")
    private Integer period;

    @Column(name = "expenditure", precision = 10, scale = 2)
    private BigDecimal expenditure;

    @Size(max = 255)
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Size(max = 255)
    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Integer isActive;
}
