package com.vts.hrms.dto;


import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EmployeeDetailsDTO implements Serializable {

    private Long empDetailId;
    private String gender;
    private String bloodGroup;
    private LocalDate dob;
    private String mobile;
    private String aadhaarNo;
    private String pan;
    private String maritalStatus;
    private int  height;
    private String motherTongue;
    private String religion;
    private String nationality;
    private String spouseName;
    private String category;
    private Long empId;

    public EmployeeDetailsDTO(Long empDetailId, String gender, String bloodGroup, LocalDate dob, String mobile, String aadhaarNo, String pan, String maritalStatus, int height, String motherTongue, String religion, String nationality, String spouseName, Long empId,String category) {
        this.empDetailId = empDetailId;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.dob = dob;
        this.mobile = mobile;
        this.aadhaarNo = aadhaarNo;
        this.pan = pan;
        this.maritalStatus = maritalStatus;
        this.height = height;
        this.motherTongue = motherTongue;
        this.religion = religion;
        this.nationality = nationality;
        this.spouseName = spouseName;
        this.empId = empId;
        this.category=category;
    }
}
