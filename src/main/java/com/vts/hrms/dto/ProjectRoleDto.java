package com.vts.hrms.dto;


import lombok.Data;


@Data
public class ProjectRoleDto {

    private Long projectId;
    private Long roleId;
    private String projectCode;
    private String roleName;
}
