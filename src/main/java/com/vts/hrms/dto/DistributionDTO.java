package com.vts.hrms.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DistributionDTO implements Serializable {

    private Long distributionId;

    private LocalDate distributionDate;

    private Long empId;

    private Long aoEmpId;

    private Long roEmpId;

    private Long projectId;

    @Size(max = 255)
    private String appointment;

    @Size(max = 1000)
    private String techActivity;

    @Size(max = 1000)
    private String nonTechActivity;

    @Size(max = 255)
    private String createdBy;

    private LocalDateTime createdDate;

    @Size(max = 255)
    private String modifiedBy;

    private LocalDateTime modifiedDate;

    private String projectCode;
    
    private String employeeName;

    private String aoOfficerName;

    private String roOfficerName;

}
