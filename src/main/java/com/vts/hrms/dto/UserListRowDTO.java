package com.vts.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListRowDTO {
    private Long loginId;
    private Long roleId;
    private Long empId;
    private Long divisionId;
    private String username;
    private String employeeName;
    private String designationName;
    private String roleName;
    private String divisionName;
}