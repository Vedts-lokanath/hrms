package com.vts.hrms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vts.hrms.dto.DivisionDTO;
import com.vts.hrms.dto.EmployeeDTO;
import com.vts.hrms.dto.RequisitionDTO;
import com.vts.hrms.entity.Course;
import com.vts.hrms.entity.Organizer;
import com.vts.hrms.entity.Requisition;
import com.vts.hrms.entity.Status;
import com.vts.hrms.mapper.FeedbackMapper;
import com.vts.hrms.mapper.RequisitionMapper;
import com.vts.hrms.repository.*;
import com.vts.hrms.util.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Value("${x_api_key}")
    private String xApiKey;

    @Value("${labCode}")
    private String labCode;

    private final EmsClientService emsClientService;
    private final ObjectMapper objectMapper;
    private final MasterClientService masterClient;
    private final OrganizerRepository organizerRepository;
    private final CourseRepository courseRepository;
    private final RequisitionMapper requisitionMapper;
    private final RequisitionRepository requisitionRepository;
    private final StatusRepository statusRepository;

//    @Cacheable(value = "getNominalROllList")
    public List<EmployeeDTO> getNominalRollList(String token) {
        log.info("Fetching nominal roll from EMS");

        try {
            ResponseEntity<ApiResponse> responseEntity = emsClientService.getNominalRollList(token);

            if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("Invalid response from EMS");
                throw new RuntimeException("Failed to fetch data from EMS");
            }

            ApiResponse apiResponse = responseEntity.getBody();

            if (apiResponse == null || !apiResponse.isSuccess()) {
                log.error("API returned failure: {}", apiResponse != null ? apiResponse.getMessage() : "null body");
                throw new RuntimeException("EMS returned failure response");
            }

            Object data = apiResponse.getData();
            if (data == null) {
                log.warn("No data found in response");
                return Collections.emptyList();
            }

            // Convert to List<EmployeeDTO>
            return objectMapper.convertValue(data, new TypeReference<List<EmployeeDTO>>() {
            });

        } catch (FeignException.Unauthorized ex) {
            log.error("Unauthorized access", ex);
            throw new RuntimeException("Unauthorized access to EMS");

        } catch (FeignException.ServiceUnavailable ex) {
            log.error("EMS service down", ex);
            throw new RuntimeException("EMS service unavailable");

        } catch (FeignException ex) {
            log.error("Feign error", ex);
            throw new RuntimeException("Error while calling EMS");

        } catch (Exception ex) {
            log.error("Unexpected error", ex);
            throw new RuntimeException("Something went wrong");
        }
    }

//    @Cacheable(value = "getCourseTrainingList")
    public List<RequisitionDTO> getCourseTrainingList() {
        log.info("Fetching course training data");
        List<Organizer> organizerList = organizerRepository.findAllByIsActive(1);
        List<Course> courseList = courseRepository.findAllByIsActive(1);
        List<Status> statusList = statusRepository.findAll();

        List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);

        Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
                .filter(e -> labCode != null && labCode.equalsIgnoreCase(e.getLabCode()))
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

        Map<Long, Organizer> organizerMap = organizerList.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        Map<Long, Course> courseMap = courseList.stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));

        Map<String, Status> statusMap = statusList.stream()
                .collect(Collectors.toMap(Status::getStatusCode, Function.identity()));

        List<Requisition> list = requisitionRepository.findAllByIsActive(1);

        List<RequisitionDTO> dtoList = requisitionMapper.toDto(list);

        dtoList.forEach(dto -> {
            EmployeeDTO employeeDTO = employeeMap.get(dto.getInitiatingOfficer());
            Course course = courseMap.get(dto.getCourseId());
            Organizer organizer = organizerMap.get(course.getOrganizerId());
            dto.setCourseName(course.getCourseName());
            dto.setVenue(course.getVenue());

            Status status = statusMap.get(dto.getStatus());
            dto.setStatusColor(status.getColorCode());
            dto.setStatusName(status.getStatusName());

            dto.setOfflineRegistrationFee(course.getOfflineRegistrationFee());
            dto.setOnlineRegistrationFee(course.getOnlineRegistrationFee());
            if (organizer != null) {
                dto.setOrganizer(organizer.getOrganizer());
                dto.setOrganizerContactName(organizer.getContactName());
                dto.setOrganizerPhoneNo(organizer.getPhoneNo());
                dto.setOrganizerFaxNo(organizer.getFaxNo());
                dto.setOrganizerEmail(organizer.getEmail());
            }
            if (employeeDTO != null) {
                dto.setEmpNo(employeeDTO.getEmpNo());
                dto.setInitiatingOfficerName(buildEmployeeName(employeeDTO, false));
                dto.setDesigCadre(employeeDTO.getDesigCadre());
                dto.setEmpDesigName(employeeDTO.getEmpDesigName());
                dto.setEmpDivCode(employeeDTO.getEmpDivCode());
                dto.setEmail(employeeDTO.getEmail());
                dto.setMobileNo(employeeDTO.getMobileNo());
            }
        });
        return dtoList;
    }

    private String buildEmployeeName(EmployeeDTO emp, boolean includeDesignation) {

        if (emp == null) return "";

        String title = Optional.ofNullable(emp.getTitle())
                .filter(t -> !t.isBlank())
                .orElse(null);

        String salutation = Optional.ofNullable(emp.getSalutation())
                .filter(s -> !s.isBlank())
                .orElse(null);

        String name = Optional.ofNullable(emp.getEmpName()).orElse("");
        String designation = Optional.ofNullable(emp.getEmpDesigName()).orElse("");

        // Priority: Title → Salutation → Nothing
        String prefix = title != null ? title : (salutation != null ? salutation : "");

        StringBuilder fullName = new StringBuilder();

        if (!prefix.isBlank()) {
            fullName.append(prefix).append(" ");
        }

        fullName.append(name);

        if (includeDesignation && !designation.isBlank()) {
            fullName.append(", ").append(designation);
        }

        return fullName.toString().trim();
    }

}
