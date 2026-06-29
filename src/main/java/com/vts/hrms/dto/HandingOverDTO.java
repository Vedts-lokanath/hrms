package com.vts.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HandingOverDTO {

    private Long handingOverId;
    private Long fromEmpId;
    private Long toEmpId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime createdDate;
    private Integer isActive;

    private String fromEmpName;
    private String toEmpName;
}
