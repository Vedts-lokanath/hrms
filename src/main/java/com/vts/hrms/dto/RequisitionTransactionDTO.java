package com.vts.hrms.dto;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RequisitionTransactionDTO implements Serializable {

    private Long transactionId;
    private Long requisitionId;
    private String statusCode;
    private String statusName;
    private Long actionBy;
    private Long actionTo;
    private LocalDateTime actionDate;
    private String remarks;

    private String colorCode;
    private String statusDetail;
    private Long forwardTo;
    private Long forwardBy;
    private String forwardByName;
    private String forwardToName;

}
