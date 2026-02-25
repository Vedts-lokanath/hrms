package com.vts.hrms.service;

import com.vts.hrms.dto.*;
import com.vts.hrms.entity.SignAuthRole;
import com.vts.hrms.entity.SignRoleAuthority;
import com.vts.hrms.mapper.SignAuthRoleMapper;
import com.vts.hrms.mapper.SignRoleAuthorityMapper;
import com.vts.hrms.repository.LoginRepository;
import com.vts.hrms.repository.SignAuthRoleRepository;
import com.vts.hrms.repository.SignRoleAuthorityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MasterService {

    private static final Logger log = LoggerFactory.getLogger(MasterService.class);

    @Value("${x_api_key}")
    private String xApiKey;

    private final MasterClientService masterClient;
    private final LoginRepository loginRepository;
    private final SignAuthRoleRepository signAuthRoleRepository;
    private final SignRoleAuthorityRepository signRoleAuthorityRepository;
    private final SignAuthRoleMapper signAuthRoleMapper;
    private final SignRoleAuthorityMapper signRoleAuthorityMapper;

    public MasterService(MasterClientService masterClient, LoginRepository loginRepository, SignAuthRoleRepository signAuthRoleRepository, SignRoleAuthorityRepository signRoleAuthorityRepository, SignAuthRoleMapper signAuthRoleMapper, SignRoleAuthorityMapper signRoleAuthorityMapper) {
        this.masterClient = masterClient;
        this.loginRepository = loginRepository;
        this.signAuthRoleRepository = signAuthRoleRepository;
        this.signRoleAuthorityRepository = signRoleAuthorityRepository;
        this.signAuthRoleMapper = signAuthRoleMapper;
        this.signRoleAuthorityMapper = signRoleAuthorityMapper;
    }

    public List<DesignationDTO> getEmpDesigMaster() {
        log.info("Fetching designation master");
        return masterClient.getEmpDesigMaster(xApiKey);
    }

    public List<DivisionDTO> getDivisionMaster() {
        log.info("Fetching division master");
        return masterClient.getDivisionMaster(xApiKey);
    }

    public LoginEmployeeDto getEmployeeByUsername(String username) {
        log.info("Fetching employee with username {}", username);

        LoginEmployeeDto dto = loginRepository.findByUserName(username);

        List<EmployeeDTO> employeeList = masterClient.getEmployeeList(xApiKey);

        Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

        EmployeeDTO employee = employeeMap.get(dto.getEmpId());

        dto.setEmpId(dto.getEmpId());
        dto.setEmpNo(employee.getEmpNo());
        dto.setEmployeeType(employee.getEmployeeType());
        dto.setTitle(employee.getTitle());
        dto.setEmpName(employee.getEmpName());
        dto.setEmpDesigName(employee.getEmpDesigName());
        dto.setEmpStatus(employee.getEmpStatus());
        dto.setRoleName(dto.getRoleName());
        dto.setDivisionId(employee.getDivisionId());
        dto.setLoginId(dto.getLoginId());
        dto.setRoleId(dto.getRoleId());

        return dto;
    }

    public List<EmployeeDTO> getEmployeeList() {
        log.info("Fetching employee master");
        return masterClient.getEmployeeList(xApiKey);
    }

    @Transactional(readOnly = true)
    public List<SignAuthRoleDTO> getSignAuthRoles(String username) {
        log.info("Request to get all SignAuthRoles by username {}", username);
        return signAuthRoleRepository
                .findAll()
                .stream()
                .map(signAuthRoleMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Transactional(readOnly = true)
    public List<SignRoleAuthorityDTO> getSignAuthorities(String username) {
        log.info("Request to get all sign role authority list by username {}", username);
        List<SignRoleAuthorityDTO> authorityDTOList = signRoleAuthorityRepository
                .findAllByIsActive(1)
                .stream()
                .map(signRoleAuthorityMapper::toDto)
                .collect(Collectors.toList());

        if (authorityDTOList.isEmpty()) {
            return authorityDTOList;
        }

        List<SignAuthRole> authRoleList = signAuthRoleRepository.findAll();
        List<EmployeeDTO> employeeList = masterClient.getEmployeeList(xApiKey);

        Map<Long, SignAuthRole> signRoleMap = authRoleList.stream()
                .collect(Collectors.toMap(SignAuthRole::getSignAuthRoleId, Function.identity()));

        Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, Function.identity()));

        for (SignRoleAuthorityDTO dto : authorityDTOList) {

            SignAuthRole role = signRoleMap.get(dto.getSignAuthRoleId());
            if (role != null) {
                dto.setSignAuthRoleDesc(role.getSignAuthRole());
            }

            EmployeeDTO employee = employeeMap.get(dto.getEmpId());
            if (employee != null) {
                dto.setEmployeeName(employee.getEmpName());
                dto.setEmployeeDesignation(employee.getEmpDesigName());
            }
        }
        return authorityDTOList;
    }

    public SignRoleAuthorityDTO addSignRoleAuthority(SignRoleAuthorityDTO dto, String username) {
        SignRoleAuthority roleAuthority = signRoleAuthorityMapper.toEntity(dto);
        roleAuthority.setCreatedBy(username);
        roleAuthority.setCreatedDate(LocalDateTime.now());
        roleAuthority.setIsActive(1);
        roleAuthority = signRoleAuthorityRepository.save(roleAuthority);
        return signRoleAuthorityMapper.toDto(roleAuthority);
    }

    public Optional<SignRoleAuthorityDTO> updateSignRoleAuthority(SignRoleAuthorityDTO dto, String username) {
        log.info("Request to update sign role authority for id {} by {}", dto.getSignRoleAuthorityId(), username);
        return signRoleAuthorityRepository
                .findById(dto.getSignRoleAuthorityId())
                .map(existingSign -> {
                    existingSign.setModifiedBy(username);
                    existingSign.setModifiedDate(LocalDateTime.now());
                    signRoleAuthorityMapper.partialUpdate(existingSign, dto);
                    return existingSign;
                })
                .map(signRoleAuthorityRepository::save)
                .map(signRoleAuthorityMapper::toDto);
    }
}
