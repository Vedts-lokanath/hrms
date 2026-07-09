package com.vts.hrms.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="hrms_cash_limit")
@Data
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CashLimit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cash_limit_id")
    private Long cashLimitId;

    @NotNull(message = "Cash Limit is required")
    @DecimalMin(value = "0.01", message = "Cash Limit must be greater than zero")
    @Column(name = "cash_limit", precision = 15, scale = 2)
    private BigDecimal cashLimit;

    @Column(name = "created_by")
    private String createdBy;

    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss a")
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_by")
    private String modifiedBy;

    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss a")
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @NotNull(message = "To Date is required")
    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "is_active")
    private Integer isActive;

}
