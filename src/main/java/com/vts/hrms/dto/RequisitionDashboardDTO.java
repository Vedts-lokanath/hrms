package com.vts.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionDashboardDTO {

    private Long organizerId;
    private String organizerName;

    private Long total;
    private Long pending;
    private Long forwarded;
    private Long recommended;
    private Long approved;


}
