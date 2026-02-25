package com.vts.hrms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class SignRoleAuthorityDTO implements Serializable {

    private Long signRoleAuthorityId;

    @NotNull(message = "Role is required")
    private Long signAuthRoleId;

    @NotNull(message = "Division is required")
    private Long divisionId;

    @NotNull(message = "Employee is required")
    private Long empId;

    private LocalDate validFrom;
    private LocalDate validUpto;

    private Long serialNo;
    private  String employeeName;
    private String signAuthRoleDesc;
    private  String employeeDesignation;
}
