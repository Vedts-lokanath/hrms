package com.vts.hrms.service;

import com.vts.hrms.dto.*;
import com.vts.hrms.entity.*;
import com.vts.hrms.exception.BadRequestException;
import com.vts.hrms.exception.NotFoundException;
import com.vts.hrms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private final RoleRepository roleRepository;
    private final LoginRepository loginRepository;
    private final RoleSecurityRepository roleSecurityRepository;
    private final MasterClientService masterClient;
    private final FormModuleRepository formModuleRepository;
    private final FormDetailRepository formDetailRepository;
    private final FormRoleAccessRepository formRoleAccessRepository;


    @Value("${x_api_key}")
    private String xApiKey;

    public AdminService(RoleRepository roleRepository, LoginRepository loginRepository, RoleSecurityRepository roleSecurityRepository, MasterClientService masterClient, FormModuleRepository formModuleRepository, FormDetailRepository formDetailRepository, FormRoleAccessRepository formRoleAccessRepository) {
        this.roleRepository = roleRepository;
        this.loginRepository = loginRepository;
        this.roleSecurityRepository = roleSecurityRepository;
        this.masterClient = masterClient;
        this.formModuleRepository = formModuleRepository;
        this.formDetailRepository = formDetailRepository;
        this.formRoleAccessRepository = formRoleAccessRepository;
    }

    public List<RoleDTO> getRoleList() {
        log.info("Fetching all roles");
        return roleRepository
                .findAll().stream()
                .map(data->{
                    RoleDTO dto = new RoleDTO();
                    dto.setRoleId(data.getRoleId());
                    dto.setRoleName(data.getRoleName());
                    return dto;
                }).toList();
    }

    public List<UserResponseDTO> getUserList() {
        log.info("Fetching all users");
        List<UserResponseDTO>  userList =loginRepository.getUserList();

        List<EmployeeDTO> employeeList = masterClient.getEmployeeList(xApiKey);

        Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

        // Set employee details into user response
        for (UserResponseDTO user : userList) {

            EmployeeDTO employee = employeeMap.get(user.getEmpId());

            if (employee != null) {
                user.setEmployeeName(employee.getEmpName());
                user.setDesignationName(employee.getEmpDesigName());
                user.setDivisionName(employee.getEmpDivCode());
            }
        }

        return userList;


    }

    public boolean checkUsernameExists(String username) {
        log.info("Request to check username : {}", username);
        return loginRepository.existsByUsernameIgnoreCase(username);
    }


    @Transactional
    public UserResponseDTO addNewUser(UserResponseDTO dto, String username) {

        log.info("Adding new user for username: {}", dto.getUsername());

        try {
            String password = "Vts@1234";

            // 1. Fetch Role
            RoleSecurity role = roleSecurityRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException(
                            "Role not found for roleId: " + dto.getRoleId()));

            // 2. Create Login Object
            Login login = new Login();
            login.setUsername(dto.getUsername());
            login.setEmpId(dto.getEmpId());
            login.setPassword(encoder.encode(password));
            login.setCreatedBy(username);
            login.setCreatedDate(LocalDateTime.now());
            login.setIsActive(1);

            // 3. Map Role ↔ Login
            Set<RoleSecurity> roleSet = new HashSet<>();
            roleSet.add(role);

            login.setRoleSecurity(roleSet);

            // 4. Save Login (join table auto insert)
            Login savedLogin = loginRepository.save(login);

            // 5. Return Response DTO
            return new UserResponseDTO(
                    savedLogin.getLoginId(),
                    role.getRoleId(),
                    savedLogin.getEmpId(),
                    dto.getDivisionId(),
                    savedLogin.getUsername(),
                    dto.getEmployeeName(),
                    dto.getDesignationName(),
                    role.getRoleName(),
                    dto.getDivisionName()
            );

        } catch (Exception e) {
            log.error("Error while adding user", e);
            throw new BadRequestException("Failed to add new user");
        }
    }

    @Transactional
    public UserResponseDTO updateUser(UserResponseDTO dto, String username) {

        log.info("Updating user with loginId: {}", dto.getLoginId());

        try {
            // 1. Fetch existing Login
            Login login = loginRepository.findById(dto.getLoginId())
                    .orElseThrow(() -> new RuntimeException(
                            "Login not found for loginId: " + dto.getLoginId()));

            // 2. Fetch Role
            RoleSecurity role = roleSecurityRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException(
                            "Role not found for roleId: " + dto.getRoleId()));

            // 3. Update login fields
            login.setUsername(dto.getUsername());
            login.setEmpId(dto.getEmpId());
            login.setModifiedBy(username);
            login.setModifiedDate(LocalDateTime.now());

            // 4. Update role mapping (join table)
            login.getRoleSecurity().clear();  // delete old mapping
            login.getRoleSecurity().add(role); // insert new mapping

            // 5. Save changes
            Login updatedLogin = loginRepository.save(login);

            // 6. Return updated DTO
            return new UserResponseDTO(
                    updatedLogin.getLoginId(),
                    role.getRoleId(),
                    updatedLogin.getEmpId(),
                    dto.getDivisionId(),
                    updatedLogin.getUsername(),
                    dto.getEmployeeName(),
                    dto.getDesignationName(),
                    role.getRoleName(),
                    dto.getDivisionName()
            );

        } catch (Exception e) {
            log.error("Error while updating user", e);
            throw new RuntimeException("Failed to update user");
        }
    }

    @Transactional
    public List<FormModuleDto> formModuleList(Long FormRoleId) throws Exception {
        log.info(" Inside formModuleList " );
        try {
            List<FormModuleDto> formModuleDtoList = new ArrayList<FormModuleDto>();
            List<FormModule> formModuleList = formModuleRepository.findDistinctFormModulesByRoleId(FormRoleId);

            formModuleList.forEach(detail -> {
                FormModuleDto formModuleDto = FormModuleDto.builder()
                        .FormModuleId(detail.getFormModuleId())
                        .FormModuleName(detail.getFormModuleName())
                        .ModuleUrl(detail.getModuleUrl())
                        .ModuleIcon(detail.getModuleIcon())
                        .SerialNo(detail.getSerialNo())
                        .IsActive(detail.getIsActive())
                        .build();

                formModuleDtoList.add(formModuleDto);
            });

            return formModuleDtoList;
        } catch (Exception e) {
            log.error(" Inside formModuleList ", e );

            return new ArrayList<FormModuleDto>();
        }
    }


    @Transactional
    public List<FormModuleDto> getformModulelist() throws Exception {
        log.info( " AdminServiceImpl Inside method getformModulelist " );
        List<FormModuleDto> FMlist = new ArrayList<FormModuleDto>();
        try {

            List<Object[]>  list =formModuleRepository.getformModulelist();
            if(list!=null) {
                for(Object[] O:list) {
                    FormModuleDto dto = new FormModuleDto();
                    dto.setFormModuleId(Long.parseLong(O[0].toString()));
                    dto.setFormModuleName(O[1].toString());
                    FMlist.add(dto);
                }
            }else {
                FMlist = null;
            }
        } catch (Exception e) {
            log.error(" error in AdminServiceImpl Inside method getformModulelist "+ e.getMessage());
            e.printStackTrace();
        }

        return FMlist;
    }


    @Transactional
    public List<FormDetailDto> formModuleDetailList(Long FormRoleId) throws Exception {
        log.info(" Inside formModuleDetailList " );
        try {
            List<FormDetailDto> formDetailDtoList = new ArrayList<FormDetailDto>();
            List<FormDetail> formDetailList = formDetailRepository.findDistinctFormModulesDetailsByRoleId(FormRoleId);

            formDetailList.forEach(detail -> {
                FormDetailDto formModuleDto = FormDetailDto.builder()
                        .FormDetailId(detail.getFormDetailId())
                        .FormModuleId(detail.getFormModuleId())
                        .FormName(detail.getFormName())
                        .FormUrl(detail.getFormUrl())
                        .FormDispName(detail.getFormDispName())
                        .FormSerialNo(detail.getFormSerialNo())
                        .FormColor(detail.getFormColor())
                        .ModifiedBy(detail.getModifiedBy())
                        .ModifiedDate(detail.getModifiedDate())
                        .IsActive(detail.getIsActive())
                        .build();

                formDetailDtoList.add(formModuleDto);
            });

            return formDetailDtoList;
        } catch (Exception e) {
            log.error(" Inside formModuleDetailList ", e );
            e.printStackTrace();
            return new ArrayList<FormDetailDto>();
        }
    }


    @Transactional
    public List<FormRoleAccessDto> getformRoleAccessList(String roleId, String formModuleId) {
        log.info( " AdminServiceImpl Inside method getformRoleAccessList");
        try {

            List<Object[]> list = formRoleAccessRepository.getformroleAccessList( roleId, formModuleId);
            return  list.stream().map(row ->{
                return FormRoleAccessDto.builder()
                        .formRoleAccessId(row[0]!=null?Long.parseLong(row[0].toString()):0L)
                        .formDetailId(row[1]!=null?Long.parseLong(row[1].toString()):0L)
                        .formModuleId(row[2]!=null?Long.parseLong(row[2].toString()):0L)
                        .formDispName(row[3]!=null?row[3].toString():null)
                        .isActive(row[4]!=null && row[4].toString().equalsIgnoreCase("1"))
                        .forView(row[5]!=null && row[5].toString().equalsIgnoreCase("Y"))
                        .forAdd(row[6]!=null && row[6].toString().equalsIgnoreCase("Y"))
                        .forEdit(row[7]!=null && row[7].toString().equalsIgnoreCase("Y"))
                        .forDelete(row[8]!=null && row[8].toString().equalsIgnoreCase("Y"))
                        .roleId(row[9]!=null?Long.parseLong(row[9].toString()):0)
                        .build();
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error(" error in AdminServiceImpl Inside method getformRoleAccessList "+ e.getMessage());
            e.printStackTrace();
            return List.of();
        }
}

    @Transactional
    public String updateformroleaccess(FormRoleAccessDto accessDto, String username) {
        log.info( " AdminServiceImpl Inside method updateformroleaccess");
        String updateResult = null;
        try {
            long result = formRoleAccessRepository.countByFormRoleIdAndDetailId(String.valueOf(accessDto.getRoleId()),String.valueOf(accessDto.getFormDetailId()));
            if(result == 0) {
                FormRoleAccess formrole = new FormRoleAccess();
                formrole.setRoleId(accessDto.getRoleId());
                formrole.setFormDetailId(accessDto.getFormDetailId());
                formrole.setIsActive(1);
                formrole.setForView(String.valueOf(accessDto.isForView()).equalsIgnoreCase("true")?"Y":"N");
                formrole.setForAdd(String.valueOf(accessDto.isForAdd()).equalsIgnoreCase("true")?"Y":"N");
                formrole.setForEdit(String.valueOf(accessDto.isForEdit()).equalsIgnoreCase("true")?"Y":"N");
                formrole.setForDelete(String.valueOf(accessDto.isForDelete()).equalsIgnoreCase("true")?"Y":"N");
                formrole.setCreatedBy(username);
                formrole.setCreatedDate(LocalDateTime.now());
                formRoleAccessRepository.save(formrole);
                updateResult = String.valueOf(formrole.getFormRoleAccessId());
            }else {
                Optional<FormRoleAccess> formRoleAccess = formRoleAccessRepository.findById(accessDto.getFormRoleAccessId());
                if(formRoleAccess.isPresent()){
                    FormRoleAccess roleAccess = formRoleAccess.get();
//                    roleAccess.setIsActive(String.valueOf(accessDto.isActive()).equalsIgnoreCase("true") ? 1 : 0);
                    roleAccess.setForView(String.valueOf(accessDto.isForView()).equalsIgnoreCase("true")?"Y":"N");
                    roleAccess.setForAdd(String.valueOf(accessDto.isForAdd()).equalsIgnoreCase("true")?"Y":"N");
                    roleAccess.setForEdit(String.valueOf(accessDto.isForEdit()).equalsIgnoreCase("true")?"Y":"N");
                    roleAccess.setForDelete(String.valueOf(accessDto.isForDelete()).equalsIgnoreCase("true")?"Y":"N");
                    roleAccess.setModifiedBy(username);
                    roleAccess.setModifiedDate(LocalDateTime.now());
                    formRoleAccessRepository.save(roleAccess);
                    updateResult = String.valueOf(roleAccess.getFormRoleAccessId());
                }
            }
            return updateResult;
        } catch (Exception e) {
            log.error(" error in AdminServiceImpl Inside method updateformroleaccess "+ e.getMessage());
            e.printStackTrace();
            return "0";
        }
    }

    public UserResponseDTO getUserById(Long loginId) {

        Login login = loginRepository.findById(loginId).orElseThrow(()-> new NotFoundException("Login data not found"));
        RoleSecurity roleSecurity = login.getRoleSecurity().stream().findAny().get();
        List<EmployeeDTO> employeeDTO = masterClient.getEmployee(xApiKey, login.getEmpId());

        return new UserResponseDTO(login.getLoginId(), roleSecurity.getRoleId(), login.getEmpId(), 0L, login.getUsername(),
                employeeDTO.get(0).getEmpName(), employeeDTO.get(0).getEmpDesigName(), roleSecurity.getRoleName(), "");
    }

    public boolean hasAccess(String username) {
        try {
            return loginRepository.existsByUsernameAndIsActive(username, 1);
        } catch (Exception e) {
            return false;
        }
    }
}
