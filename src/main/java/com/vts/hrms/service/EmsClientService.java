package com.vts.hrms.service;

import com.vts.hrms.dto.EmployeeDTO;
import com.vts.hrms.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "emsClient", url = "${ems_feign_client_uri}")
public interface EmsClientService {

    @GetMapping( value = "/api/reports/nominal-roll", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> getNominalRollList(@RequestHeader("Authorization") String token);

}
