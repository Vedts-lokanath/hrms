package com.vts.hrms.controller;

import com.vts.hrms.dto.CourseDashboardDTO;
import com.vts.hrms.dto.RequisitionDTO;
import com.vts.hrms.dto.RequisitionDashboardDTO;
import com.vts.hrms.service.DashboardService;
import com.vts.hrms.service.TrainingService;
import com.vts.hrms.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final TrainingService trainingService;

    public DashboardController(DashboardService dashboardService, TrainingService trainingService) {
        this.dashboardService = dashboardService;
        this.trainingService = trainingService;
    }

    @GetMapping("/course-count")
    public ResponseEntity<List<CourseDashboardDTO>> getOrganizerCourseDashboard(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<CourseDashboardDTO> response =
                dashboardService.getOrganizerCourseDashboard(startDate, endDate);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/requisition-count")
    public ResponseEntity<List<RequisitionDashboardDTO>> getOrganizerRequisitionDashboard() {

        List<RequisitionDashboardDTO> response =
                dashboardService.getOrganizerRequisitionDashboard();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/requisition-filter")
    public ResponseEntity<List<RequisitionDashboardDTO>> getRequisitionFilterDashboard(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<RequisitionDashboardDTO> response =
                dashboardService.getRequisitionFilterDashboard(startDate, endDate);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/requisition-list")
    public ResponseEntity<List<RequisitionDTO>> getRequisitionList(@RequestParam Long empId, @RequestParam String roleName,
                                                                   @RequestHeader(value = "username", required = false) String username) {
        List<RequisitionDTO> list = trainingService.getRequisitionList(empId, roleName, username);

        return ResponseEntity.ok(list);
    }

    @GetMapping("/user-requisition-filter")
    public ResponseEntity<List<RequisitionDashboardDTO>> getUserRequisitionFilter(
            @RequestParam Long empId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<RequisitionDashboardDTO> response =
                dashboardService.getUserRequisitionFilter(empId, startDate, endDate);

        return ResponseEntity.ok(response);
    }

}
