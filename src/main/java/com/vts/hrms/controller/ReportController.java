package com.vts.hrms.controller;

import com.vts.hrms.dto.CepDTO;
import com.vts.hrms.dto.EmployeeDTO;
import com.vts.hrms.dto.RequisitionDTO;
import com.vts.hrms.dto.SponsorshipDTO;
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
        List<RequisitionDTO> list = reportsService.getCourseTrainingList("course");
        return ResponseEntity.ok(
                new ApiResponse(true, "Course training list fetched successfully", list)
        );
    }

    @GetMapping("/seminar-training")
    public ResponseEntity<ApiResponse> getSeminarTrainingData() {
        LOG.info(" Request to fetch get seminar training list");
        List<RequisitionDTO> list = reportsService.getCourseTrainingList("seminar");
        return ResponseEntity.ok(
                new ApiResponse(true, "Seminar training list fetched successfully", list)
        );
    }

    @GetMapping("/cep")
    public ResponseEntity<ApiResponse> getCEPData(@RequestHeader String username) {
        LOG.info(" Request to fetch get CEP list");
        List<CepDTO> list = reportsService.getCEPData(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "CEP list fetched successfully", list)
        );
    }

    @GetMapping("/sponsor-mtech")
    public ResponseEntity<ApiResponse> getMTechData(@RequestHeader String username) {
        LOG.info(" Request to fetch get Sponsorship M.Tech list");
        List<SponsorshipDTO> list = reportsService.getMTechData(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Sponsorship M.Tech list fetched successfully", list)
        );
    }

    @GetMapping("/sponsor-phd")
    public ResponseEntity<ApiResponse> getPhdData(@RequestHeader String username) {
        LOG.info(" Request to fetch get Sponsorship Phd list");
        List<SponsorshipDTO> list = reportsService.getPhdData(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Sponsorship Phd list fetched successfully", list)
        );
    }

}
