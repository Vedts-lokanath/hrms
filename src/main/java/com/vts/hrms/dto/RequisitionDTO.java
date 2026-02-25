package com.vts.hrms.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionDTO implements Serializable {

    private Long requisitionId;

    @NotNull(message = "Program is required")
    private Long programId;

    @NotNull(message = "Initiating Officer is required")
    private Long initiatingOfficer;

    @NotNull(message = "From date is required")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    private LocalDate toDate;

    @NotNull(message = "Duration is required")
    private Integer duration;

    @NotBlank(message = "Mode of payment is required")
    private String modeOfPayment;

    @NotBlank(message = "Course submission is required")
    private String isSubmitted;

    private String reference;
    private String venue;
    private String necessity;
    private BigDecimal registrationFee;

    private String status;
    private String fileEcs;
    private String fileCheque;
    private String filePan;
    private String fileBrochure;

    private String programName;
    private Long organizerId;
    private String organizer;
    private String organizerContactName;
    private String organizerPhoneNo;
    private String organizerFaxNo;
    private String organizerEmail;

    private String initiatingOfficerName;
    private String empNo;
    private String empDesigName;
    private String empDivCode;
    private String email;
    private String mobileNo;

    private MultipartFile multipartFileEcs;
    private MultipartFile multipartFileCheque;
    private MultipartFile multipartFilePan;
    private MultipartFile multipartFileBrochure;
}
