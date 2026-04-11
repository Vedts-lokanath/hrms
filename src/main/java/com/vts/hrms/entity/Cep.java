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
@Table(name = "hrms_cep")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Cep implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cep_id")
    private Long cepId;

    @Column(name = "division_id")
    private Long divisionId;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "no_of_participants")
    private Long noOfParticipants;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount_spent", precision = 10, scale = 2)
    private BigDecimal amountSpent;

    @Size(max = 2000)
    @Column(name = "comments", length = 2000)
    private String comments;

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
