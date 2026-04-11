package com.vts.hrms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectMasterDTO {

    private Long projectId;
    private String labCode;
    private Long projectMainId;
    private String projectCode;
    private String projectShortName;
    private String projectImmsCd;
    private String projectName;
    private String projectDescription;
    private String unitCode;
    private String projectType;
    private String projectDirectorName;
    private Long projectCategory;
    private String sanctionNo;
    private LocalDate sanctionDate;
    private Double totalSanctionCost;
    private Double sanctionCostRE;
    private Double sanctionCostFE;
    private LocalDate pdc;
    private Long projectDirector;
    private String projSancAuthority;
    private String boardReference;
    private Integer revisionNo;
    private Integer isMainWC;
    private String workCenter;
    private String objective;
    private String deliverable;
    private String endUser;
    private String application;
    private String labParticipating;
    private String scope;
    private int divisionId;
    private String authorityNo;
    private String authorityDate;
    private String createdBy;
    private int isActive;
    private String modifiedBy;
    private LocalDateTime createdDate;
    private String prjDirectorName;
    private String prjDirectorDesig;

}
