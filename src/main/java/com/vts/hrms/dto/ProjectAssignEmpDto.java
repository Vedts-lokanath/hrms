package com.vts.hrms.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectAssignEmpDto {

    private Long empId;
    private List<ProjectRoleDto> dtoList;

}
