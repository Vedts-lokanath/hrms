package com.vts.hrms.controller;

import com.vts.hrms.dto.*;
import com.vts.hrms.service.MasterService;
import com.vts.hrms.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/master")
public class MasterController {

    private final MasterService masterService;

    public MasterController( MasterService masterService) {
        this.masterService = masterService;
    }

    @GetMapping(value = "/designation")
    public ResponseEntity<ApiResponse> getAllDesignation() {
        List<DesignationDTO> list = masterService.getEmpDesigMaster();

        return ResponseEntity.ok(
                new ApiResponse(true, "Designation list fetched", list)
        );
    }

    @GetMapping(value = "/division")
    public ResponseEntity<ApiResponse> getAllDivision() {
        List<DivisionDTO> list = masterService.getDivisionMaster();

        return ResponseEntity.ok(
                new ApiResponse(true, "Division list fetched", list)
        );
    }

    @GetMapping("/getUser/{username}")
    public ResponseEntity<ApiResponse> getEmployeeByUsername(@PathVariable String username) {
        LoginEmployeeDto employee = masterService.getEmployeeByUsername(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Employee fetched by username", employee)
        );
    }

    @GetMapping(value = "/employee")
    public ResponseEntity<ApiResponse> getAllEmployees(@RequestParam("empId") Long empId, @RequestParam("roleName") String roleName) {


        List<EmployeeDTO> employeeList = masterService.getEmployeeList();

        List<EmployeeDTO> list;


        if ("ROLE_ADMIN".equalsIgnoreCase(roleName)) {

            // Admin → Get full list
            list = employeeList.stream()
                    .sorted(Comparator.comparing(EmployeeDTO::getSrNo))
                    .collect(Collectors.toList());

        } else if ("ROLE_USER".equalsIgnoreCase(roleName)) {

            // User → Filter only their own empId
            list = employeeList.stream()
                    .filter(emp -> emp.getEmpId().equals(empId))
                    .sorted(Comparator.comparing(EmployeeDTO::getSrNo))
                    .collect(Collectors.toList());

        } else {

            // Optional: default case
            list = Collections.emptyList();
        }

        return ResponseEntity.ok(
                new ApiResponse(true, "Employee list fetched successfully", list)
        );
    }

    @GetMapping(value = "/sign-auth-roles")
    public ResponseEntity<ApiResponse> getSignAuthRoles(@RequestHeader String username) {
        List<SignAuthRoleDTO> list = masterService.getSignAuthRoles(username);

        return ResponseEntity.ok(
                new ApiResponse(true, "Sign Auth Role list fetched", list)
        );
    }

    @GetMapping(value = "/sign-authority")
    public ResponseEntity<ApiResponse> getSignAuthorities(@RequestHeader String username) {
        List<SignRoleAuthorityDTO> list = masterService.getSignAuthorities(username);

        return ResponseEntity.ok(
                new ApiResponse(true, "Sign Role Authority list fetched", list)
        );
    }

    @PostMapping(value = "/add-sign-authority")
    public ResponseEntity<ApiResponse> addProgramData(@RequestBody SignRoleAuthorityDTO dto, @RequestHeader String username) {
        SignRoleAuthorityDTO data = masterService.addSignRoleAuthority(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Sign Role Authority added successfully", data)
        );
    }

    @PutMapping(value = "/update-sign-authority")
    public ResponseEntity<ApiResponse> updateProgramData(@RequestBody SignRoleAuthorityDTO dto, @RequestHeader String username) {
        Optional<SignRoleAuthorityDTO> data = masterService.updateSignRoleAuthority(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Sign Role Authority updated successfully", data)
        );
    }

}
