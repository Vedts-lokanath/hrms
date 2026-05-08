package com.vts.hrms.controller;

import com.vts.hrms.dto.*;
import com.vts.hrms.service.ReportService;
import com.vts.hrms.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportsService;

    @GetMapping("/nominal-roll")
    public ResponseEntity<ApiResponse> getTotalSisDashboardData(@RequestHeader("Authorization") String token) {
        LOG.info(" REST request to fetch nominal roll list");
        List<EmployeeDTO> list = reportsService.getNominalRollList(token);
        return ResponseEntity.ok(
                new ApiResponse(true, "Nominal roll list fetched successfully", list)
        );
    }

    @GetMapping("/course-training")
    public ResponseEntity<ApiResponse> getCourseTrainingData() {
        LOG.info(" Request to fetch course training list");
        List<RequisitionDTO> list = reportsService.getCourseTrainingList("course");
        return ResponseEntity.ok(
                new ApiResponse(true, "Course training list fetched successfully", list)
        );
    }

    @GetMapping("/seminar-training")
    public ResponseEntity<ApiResponse> getSeminarTrainingData() {
        LOG.info(" Request to fetch seminar training list");
        List<RequisitionDTO> list = reportsService.getCourseTrainingList("seminar");
        return ResponseEntity.ok(
                new ApiResponse(true, "Seminar training list fetched successfully", list)
        );
    }

    @GetMapping("/cep")
    public ResponseEntity<ApiResponse> getCEPData(@RequestParam LocalDate fromDate, @RequestParam LocalDate toDate, @RequestHeader String username) {
        LOG.info(" Request to fetch CEP list for period {} - {} ", fromDate, toDate);
        List<CepDTO> list = reportsService.getCEPData(fromDate, toDate, username);
        return ResponseEntity.ok(
                new ApiResponse(true, "CEP list fetched successfully", list)
        );
    }

    @GetMapping("/sponsor-mtech")
    public ResponseEntity<ApiResponse> getMTechData(@RequestHeader String username) {
        LOG.info(" Request to fetch Sponsorship M.Tech list");
        List<SponsorshipDTO> list = reportsService.getMTechData(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Sponsorship M.Tech list fetched successfully", list)
        );
    }

    @GetMapping("/sponsor-phd")
    public ResponseEntity<ApiResponse> getPhdData(@RequestHeader String username) {
        LOG.info(" Request to fetch Sponsorship Phd list");
        List<SponsorshipDTO> list = reportsService.getPhdData(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Sponsorship Phd list fetched successfully", list)
        );
    }

    @GetMapping("/hr-distribution")
    public ResponseEntity<ApiResponse> getHrDistributionData(@RequestHeader String username) {
        LOG.info(" Request to fetch HR distribution list");
        List<DistributionDTO> list = reportsService.getHrDistributionData(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "HR distribution list fetched successfully", list)
        );
    }

    @GetMapping("/annual-training")
    public ResponseEntity<ApiResponse> getAnnualTrainingReport(@RequestHeader String username) {
        LOG.info(" Request to fetch annual training report list");
        List<AnnualTrainingReportDTO> list = reportsService.getAnnualTrainingReport(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Annual training report list fetched successfully", list)
        );
    }

    @GetMapping("/budget-expenditure")
    public ResponseEntity<ApiResponse> getBudgetExpenditureReport(@RequestParam LocalDate fromDate, @RequestParam LocalDate toDate, @RequestHeader String username) {
        LOG.info(" Request to fetch budget expenditure report list for period {} - {} ", fromDate, toDate);
        List<BudgetExpenditureDTO> list = reportsService.getBudgetExpenditureReport(fromDate, toDate, username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Budget expenditure report list fetched successfully", list)
        );
    }

    @GetMapping("/gender-budgeting")
    public ResponseEntity<ApiResponse> getGenderBudgetReport(@RequestHeader String username) {
        LOG.info(" Request to fetch gender budgeting report list");
        List<GenderBudgetDTO> data = reportsService.getGenderBudgetReport(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Gender budget report list fetched successfully", data)
        );
    }

    @GetMapping("/training-scst")
    public ResponseEntity<ApiResponse> getTrainingSCSTReport(@RequestHeader("Authorization") String token,@RequestHeader String username) {
        LOG.info(" Request to fetch training scst report list");
        List<TrainingSCSTDTO> data = reportsService.getTrainingSCSTReport(token,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Training SC/ST report list fetched successfully", data)
        );
    }

    @GetMapping("/research-paper-int")
    public ResponseEntity<ApiResponse> getResearchPaperIntReport(@RequestHeader String username) {
        LOG.info(" Request to fetch research paper international report list");
        List<JournalDTO> data = reportsService.getResearchPaperIntReport(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Research paper int. report list fetched successfully", data)
        );
    }

}
