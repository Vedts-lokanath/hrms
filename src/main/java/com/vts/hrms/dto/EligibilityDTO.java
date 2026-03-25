package com.vts.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class EligibilityDTO implements Serializable {

    private Long eligibilityId;

    @NotBlank(message = "Eligibility name is required")
    private String eligibilityName;

    private LocalDateTime createdDate;

}
