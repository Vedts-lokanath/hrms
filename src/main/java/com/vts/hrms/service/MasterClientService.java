package com.vts.hrms.service;

import com.vts.hrms.auth.AuthenticationRequest;
import com.vts.hrms.dto.DesignationDTO;
import com.vts.hrms.dto.DivisionDTO;
import com.vts.hrms.dto.EmployeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "masterClient", url = "${feign_client_uri}")
public interface MasterClientService {


    @GetMapping("/getEmpDesigMaster")
    List<DesignationDTO> getEmpDesigMaster(@RequestHeader("X-API-KEY") String apiKey);

    @GetMapping("/getDivisionMaster")
    List<DivisionDTO> getDivisionMaster(@RequestHeader("X-API-KEY") String apiKey);

    @GetMapping("/getEmployee")
    List<EmployeeDTO> getEmployeeList(@RequestHeader("X-API-KEY") String apiKey);


    @GetMapping("/getEmployee")
    List<EmployeeDTO> getEmployee(@RequestHeader("X-API-KEY") String apiKey, @RequestParam("empId") long empId);

    @RequestMapping(value = "/getAuthenticate", method = RequestMethod.POST)
    ResponseEntity<String> getAuthenticate(@RequestBody AuthenticationRequest authenticationRequest);
}
