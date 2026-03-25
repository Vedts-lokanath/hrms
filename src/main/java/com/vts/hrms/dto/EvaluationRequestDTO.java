package com.vts.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationRequestDTO implements Serializable {

    private Long initiator;
    private String empName;
    private String designation;
    private String title;
    private List<EvaluationDTO> evaluation;
    private EvaluationDTO evaluationData;
}
