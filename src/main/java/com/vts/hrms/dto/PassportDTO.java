package com.vts.hrms.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class PassportDTO implements Serializable {

    private Long passportId;
    private Long empId;
    private String passportType;
    private String passportNo;
    private LocalDate passportIssueDate;
    private String passportIssuePlace;
    private LocalDate passportExpiryDate;

    public PassportDTO(Long passportId, Long empId, String passportType, String passportNo, LocalDate passportIssueDate, String passportIssuePlace, LocalDate passportExpiryDate) {
        this.passportId = passportId;
        this.empId = empId;
        this.passportType = passportType;
        this.passportNo = passportNo;
        this.passportIssueDate = passportIssueDate;
        this.passportIssuePlace = passportIssuePlace;
        this.passportExpiryDate = passportExpiryDate;
    }
}


