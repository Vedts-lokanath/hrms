package com.vts.hrms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "hrms_requisition_sequence")
public class RequisitionSequence {

    @Id
    @Column(name = "financial_year")
    private String financialYear;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber;
}