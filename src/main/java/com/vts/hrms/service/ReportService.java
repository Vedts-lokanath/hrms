package com.vts.hrms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vts.hrms.dto.*;
import com.vts.hrms.entity.Course;
import com.vts.hrms.entity.Organizer;
import com.vts.hrms.entity.Requisition;
import com.vts.hrms.entity.Status;
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
    private final RequisitionMapper requisitionMapper;
    private final RequisitionRepository requisitionRepository;
    private final MasterCacheService masterCacheService;
    private final TrainingService trainingService;
    private final SponsorshipService sponsorshipService;

    @Cacheable(value = "getNominalROllList")
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
            return objectMapper.convertValue(data, new TypeReference<List<EmployeeDTO>>() {});

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
    public List<RequisitionDTO> getCourseTrainingList(String courseType) {
        log.info("Fetching course training data");

        Map<Long, EmployeeDTO> employeeMap = masterCacheService.getLongEmployeeDTOMap();
        Map<Long, Organizer> organizerMap = masterCacheService.getOrganizerMap();
        Map<Long, Course> courseMap = masterCacheService.getCourseMap();
        Map<String, Status> statusMap = masterCacheService.getStatusMap();

        List<CourseTypeDTO> typeDTOList = trainingService.getCourseTypeList("user");
        Map<Long, CourseTypeDTO> typeDTOMap = typeDTOList.stream()
                .collect(Collectors.toMap(CourseTypeDTO::getCourseTypeId, Function.identity()));

        List<Requisition> list = requisitionRepository.findAllByIsActive(1);

        boolean isTraining = "course".equalsIgnoreCase(courseType);

        return list.stream()
                .map(requisitionMapper::toDto)
                .filter(dto -> {
                    // Pre-fetch the course to determine the type for filtering
                    Course course = courseMap.get(dto.getCourseId());
                    if (course == null) return false;

                    CourseTypeDTO typeDTO = typeDTOMap.get(course.getCourseTypeId());
                    String typeName = (typeDTO != null) ? typeDTO.getCourseType() : "";

                    // Keep the dto if it matches the criteria
                    return isTraining ? "Training".equalsIgnoreCase(typeName)
                            : !"Training".equalsIgnoreCase(typeName);
                })
                .peek(dto -> {

                    Course course = courseMap.get(dto.getCourseId());
                    Organizer organizer = organizerMap.get(course.getOrganizerId());
                    CourseTypeDTO typeDTO = typeDTOMap.get(course.getCourseTypeId());
                    EmployeeDTO employeeDTO = employeeMap.get(dto.getInitiatingOfficer());
                    Status status = statusMap.get(dto.getStatus());

                    dto.setCourseName(course.getCourseName());
                    dto.setCourseLevel(course.getCourseLevel());
                    dto.setCourseType(typeDTO.getCourseType());
                    dto.setVenue(course.getVenue());

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
                })
                .toList();
    }

    public List<CepDTO> getCEPData(String username) {
        log.info("Fetching CEP list data");
        return trainingService.getAllCepData(username);
    }

    public List<SponsorshipDTO> getMTechData(String username) {
        log.info("Fetching Sponsorship M.Tech list data");
        return sponsorshipService.getAllSponsorshipList("MTECH",username);
    }

    public List<SponsorshipDTO> getPhdData(String username) {
        log.info("Fetching Sponsorship Phd list data");
        return sponsorshipService.getAllSponsorshipList("PHD",username);
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
