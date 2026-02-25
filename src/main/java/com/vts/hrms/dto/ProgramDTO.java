package com.vts.hrms.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProgramDTO implements Serializable {

    private Long programId;

    @NotBlank(message = "Program Name is required")
    private String programName;

    @NotNull(message = "Organizer is required")
    private Long organizerId;
    private String organizer;

    @NotNull(message = "Registration Fee is required")
    private BigDecimal registrationFee;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "From date is required")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    private LocalDate toDate;

}
