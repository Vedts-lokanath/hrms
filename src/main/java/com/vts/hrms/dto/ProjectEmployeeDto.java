package com.vts.hrms.dto;
import lombok.Data;

@Data

public class ProjectEmployeeDto {

    private Long projectEmployeeId;
    private Long empId;
    private String projectCode;
    private Long projectId;
    private String role;
    private Long roleMasterId;


}
