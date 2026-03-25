package com.vts.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDashboardDTO {

    private Long organizerId;
    private String organizerName;
    private Long courseCount;

}
