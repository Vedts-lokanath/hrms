package com.vts.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmployeeDTO implements Serializable {

    private Long empId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Employee code is required")
    private String empNo;

    @NotBlank(message = "Employee type is required")
    private String employeeType;

    @NotBlank(message = "Full name is required")
    private String empName;

    @NotNull(message = "DesignationId is required")
    private Long designationId;

    @NotNull(message = "DivisionId is required")
    private Long divisionId;

    @NotBlank(message = "Employee status is required")
    private String empStatus;

    private Long srNo;
    private String empDesigName;
    private String empDesigCode;
    private String designationCadre;
    private String empDivCode;
    private String roleName;
    private String maritalStatus;
    private String gender;
    private String email;
    private String mobileNo;

    public EmployeeDTO(String title, Long empId, String empNo, String employeeType, String empName, Long designationId, Long divisionId, String empStatus) {
        this.title = title;
        this.empId = empId;
        this.empNo = empNo;
        this.employeeType = employeeType;
        this.empName = empName;
        this.designationId = designationId;
        this.divisionId = divisionId;
        this.empStatus = empStatus;
    }
}
