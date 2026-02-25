package com.vts.hrms.controller;


import com.vts.hrms.auth.AuthenticationController;
import com.vts.hrms.dto.*;
import com.vts.hrms.service.AdminService;
import com.vts.hrms.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin")
public class AdminController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    @GetMapping(value = "/roles")
    public ResponseEntity<ApiResponse> getRoleList() {
        List<RoleDTO> list = adminService.getRoleList();

        return ResponseEntity.ok(
                new ApiResponse(true, "Role list fetched", list)
        );
    }

    @GetMapping(value = "/user-list")
    public ResponseEntity<ApiResponse> getAllUsersList() {
        List<UserResponseDTO> list = adminService.getUserList();

        return ResponseEntity.ok(
                new ApiResponse(true, "User list fetched", list)
        );
    }

    @GetMapping(value = "/user/{loginId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long loginId) {
        UserResponseDTO dto = adminService.getUserById(loginId);

        return ResponseEntity.ok(
                new ApiResponse(true, "user data fetched", dto)
        );
    }

    @GetMapping("/exists/{username}")
    public boolean existsUsername(@PathVariable String username) {
        return adminService.checkUsernameExists(username);
    }

    @PostMapping(value = "/add-user")
    public ResponseEntity<ApiResponse> addNewUser(@RequestBody UserResponseDTO dto, @RequestHeader String username) {
        UserResponseDTO saved = adminService.addNewUser(dto, username);
        return ResponseEntity.ok(
                new ApiResponse(true, "User added successfully", saved)
        );
    }

    @PatchMapping(value = "/update-user/{loginId}")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody UserResponseDTO dto, @RequestHeader String username) {
        UserResponseDTO saved = adminService.updateUser(dto, username);
        return ResponseEntity.ok(
                new ApiResponse(true, "User updated successfully", saved)
        );
    }

    @RequestMapping (value="/header-module", method=RequestMethod.POST,produces="application/json")
    public List<FormModuleDto> headerModule(@RequestBody Long FormRoleId) throws Exception {

        return adminService.formModuleList(FormRoleId);
    }

    @RequestMapping (value="/header-detail", method=RequestMethod.POST,produces="application/json")
    public List<FormDetailDto> headerDetail(@RequestBody Long FormRoleId) throws Exception {

        return adminService.formModuleDetailList(FormRoleId);
    }

    @PostMapping(value = "/form-modules-list", produces="application/json")
    public ResponseEntity<List<FormModuleDto>> formModule()throws Exception{

        List<FormModuleDto> list = null;

        try {
            list =  adminService.getformModulelist();
        } catch (Exception e) {

            e.printStackTrace();
        }

        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping(value = "form-role-access-list", produces = "application/json")
    public ResponseEntity<List<FormRoleAccessDto>> formRoleAccessList(@RequestBody Map<String, String> request) throws Exception {

        List<FormRoleAccessDto> list = null;

        try {
            String roleId = request.get("roleId");
            String formModuleId = request.get("formModuleId");
            list = adminService.getformRoleAccessList(roleId, formModuleId);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(List.of(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping(value="update-form-role-access", produces="application/json")
    public String updateFormRoleAccess(@RequestBody FormRoleAccessDto accessDto, @RequestHeader  String username) throws Exception {

        String result=null;
        try {
            result = adminService.updateformroleaccess(accessDto, username);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }

    @GetMapping(value="/user-login-access")
    public ResponseEntity<Boolean> checkAccess(@RequestHeader String username) {
        LOG.info(" REST request to check access: {}", username);
        try {
            boolean hasAccess = adminService.hasAccess(username);
            return ResponseEntity.ok(hasAccess);
        } catch (Exception e) {
            LOG.error("Error while fetching user access {}", e.getMessage(), e);
            return ResponseEntity.ok(false);
        }
    }

}
