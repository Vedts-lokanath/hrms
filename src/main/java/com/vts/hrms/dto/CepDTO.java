package com.vts.hrms.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CepDTO implements Serializable {

    private Long cepId;
    private String divisionCode;

    private Long divisionId;

    private LocalDate fromDate;

    private LocalDate toDate;

    private Long duration;

    private Long noOfParticipants;

    private BigDecimal totalAmount;

    private BigDecimal amountSpent;

    private String comments;

    private String createdBy;

    private LocalDateTime createdDate;

    private String modifiedBy;

    private LocalDateTime modifiedDate;

    private Integer isActive;
}
