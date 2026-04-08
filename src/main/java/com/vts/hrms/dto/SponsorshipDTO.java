package com.vts.hrms.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SponsorshipDTO implements Serializable {

    private Long sponsorshipId;

    @NotNull(message = "Employee Id is required")
    private Long empId;
    private String employeeName;

    @NotBlank(message = "Degree Type is required")
    private String degreeType;

    private String delegatedPower;

    private String discipline;

    private String subject;

    private String university;

    private String preference;

    private String city;

    private LocalDate fromDate;

    private LocalDate toDate;

    private Integer period;

    private BigDecimal expenditure;
}