package com.vts.hrms.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class LabDetailsDTO implements Serializable {

    private Long empLabDetailId;
    private Long empId;
    private String presentLab;
    private String presentLabCity;
    private String presentLabState;
    private String presentPostingLab;
    private String dronaEmail;
    private String internetEmail;
    private String quota;
    private String appointment;
    private String grade;
    private String gpfNo;
    private String pranNo;
    private String cghsNo;
    private String gradeOfJoiningDRDO;
    private String initialPostingLab;
    private LocalDate dateOfJoiningDRDO;
    private LocalDate dateOfJoiningDRDOHq;
    private LocalDate dateOfJoiningPresentLab;
    private LocalDate dateOfJoiningPresentGrade;
    private LocalDate dateOfJoiningGovtService;


    public LabDetailsDTO(Long empLabDetailId, Long empId, String presentLab, String presentLabCity, String presentLabState, String presentPostingLab, String dronaEmail, String internetEmail, String quota, String appointment, String grade, String gpfNo, String pranNo, String cghsNo, String gradeOfJoiningDRDO, String initialPostingLab, LocalDate dateOfJoiningDRDO, LocalDate dateOfJoiningDRDOHq, LocalDate dateOfJoiningPresentLab, LocalDate dateOfJoiningPresentGrade, LocalDate dateOfJoiningGovtService) {
        this.empLabDetailId = empLabDetailId;
        this.empId = empId;
        this.presentLab = presentLab;
        this.presentLabCity = presentLabCity;
        this.presentLabState = presentLabState;
        this.presentPostingLab = presentPostingLab;
        this.dronaEmail = dronaEmail;
        this.internetEmail = internetEmail;
        this.quota = quota;
        this.appointment = appointment;
        this.grade = grade;
        this.gpfNo = gpfNo;
        this.pranNo = pranNo;
        this.cghsNo = cghsNo;
        this.gradeOfJoiningDRDO = gradeOfJoiningDRDO;
        this.initialPostingLab = initialPostingLab;
        this.dateOfJoiningDRDO = dateOfJoiningDRDO;
        this.dateOfJoiningDRDOHq = dateOfJoiningDRDOHq;
        this.dateOfJoiningPresentLab = dateOfJoiningPresentLab;
        this.dateOfJoiningPresentGrade = dateOfJoiningPresentGrade;
        this.dateOfJoiningGovtService = dateOfJoiningGovtService;
    }
}

