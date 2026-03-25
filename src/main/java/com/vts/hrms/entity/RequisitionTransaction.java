package com.vts.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "hrms_req_transaction")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RequisitionTransaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "requisition_id")
    private Long requisitionId;

    @Column(name = "status_code")
    private String statusCode;

    @Column(name = "action_by")
    private Long actionBy;

    @Column(name = "action_to")
    private Long actionTo;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "is_active")
    private int isActive;

}
