package com.vts.hrms.controller;

import com.vts.hrms.dto.CourseDashboardDTO;
import com.vts.hrms.dto.RequisitionDashboardDTO;
import com.vts.hrms.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
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

}
