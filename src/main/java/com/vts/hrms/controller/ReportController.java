package com.vts.hrms.controller;

import com.vts.hrms.dto.EmployeeDTO;
import com.vts.hrms.dto.RequisitionDTO;
import com.vts.hrms.service.ReportService;
import com.vts.hrms.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportsService;

    @GetMapping("/nominal-roll")
    public ResponseEntity<ApiResponse> getTotalSisDashboardData(@RequestHeader("Authorization") String token) {
        LOG.info(" REST request to fetch get nominal roll list");
        List<EmployeeDTO> list = reportsService.getNominalRollList(token);
        return ResponseEntity.ok(
                new ApiResponse(true, "Nominal roll list fetched successfully", list)
        );
    }

    @GetMapping("/course-training")
    public ResponseEntity<ApiResponse> getCourseTrainingData() {
        LOG.info(" Request to fetch get course training list");
        List<RequisitionDTO> list = reportsService.getCourseTrainingList();
        return ResponseEntity.ok(
                new ApiResponse(true, "Course training list fetched successfully", list)
        );
    }

}
