package com.vts.hrms.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;

@Data
public class SignAuthRoleDTO implements Serializable {

    private Long signAuthRoleId;

    private Long formModuleId;

    @Size(max = 12)
    private String signAuthCode;

    @Size(max = 20)
    private String signAuthRole;

    private Integer serialNo;

}
