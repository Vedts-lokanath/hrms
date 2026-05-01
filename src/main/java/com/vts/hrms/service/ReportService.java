package com.vts.hrms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vts.hrms.dto.*;
import com.vts.hrms.entity.*;
import com.vts.hrms.mapper.RequisitionMapper;
import com.vts.hrms.repository.*;
import com.vts.hrms.util.ApiResponse;
import com.vts.hrms.util.CommonUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
    private final CourseRepository courseRepository;
    private final CourseTypeRepository courseTypeRepository;
    private final SponsorshipRepository sponsorshipRepository;

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
                        dto.setInitiatingOfficerName(CommonUtil.buildEmployeeName(employeeDTO, false));
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
        return sponsorshipService.getAllSponsorshipList("MTECH", username);
    }

    public List<SponsorshipDTO> getPhdData(String username) {
        log.info("Fetching Sponsorship Phd list data");
        return sponsorshipService.getAllSponsorshipList("PHD", username);
    }

    public List<DistributionDTO> getHrDistributionData(String username) {
        log.info("Fetching HR distribution list data");

        List<DistributionDTO> dtoList = trainingService.getAllDistributionsData(username);
        Map<Long, List<ProjectEmployeeDto>> empProjectMap = masterCacheService.getProjectEmployeeMap();

        dtoList.forEach(data -> {

            List<ProjectEmployeeDto> projectList =
                    empProjectMap.getOrDefault(data.getEmpId(), Collections.emptyList());

            if (projectList != null && !projectList.isEmpty()) {

                List<ProjectRoleDto> roleDtoList = projectList.stream()
                        .map(p -> {
                            ProjectRoleDto dto = new ProjectRoleDto();
                            dto.setProjectCode(p.getProjectCode());
                            dto.setRoleName(p.getRole());
                            return dto;
                        })
                        .collect(Collectors.toList());
                data.setRoleDtoList(roleDtoList);

            } else {
                data.setRoleDtoList(Collections.emptyList());
            }
        });
        return dtoList;
    }

    public List<AnnualTrainingReportDTO> getAnnualTrainingReport(String username) {
        log.info("Fetching Annual training report list data");

        List<Requisition> requisitions = requisitionRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<CourseType> courseTypes = courseTypeRepository.findAll();

        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));

        Map<Long, CourseType> courseTypeMap = courseTypes.stream()
                .collect(Collectors.toMap(CourseType::getCourseTypeId, Function.identity()));

        Map<Long, EmployeeDTO> employeeMap = masterCacheService.getLongEmployeeDTOMap();

        // Create fixed rows
        Map<String, AnnualTrainingReportDTO> rowMap = new LinkedHashMap<>();

        rowMap.put("CEP", new AnnualTrainingReportDTO("CEP", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L,
                0L, 0L));

        rowMap.put("Special", new AnnualTrainingReportDTO("Special", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L,
                0L, 0L));

        rowMap.put("Targeted", new AnnualTrainingReportDTO("Targeted", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L,
                0L, 0L));

        rowMap.put("Foreign", new AnnualTrainingReportDTO("Foreign", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L,
                0L, 0L));

        rowMap.put("Techno Managerial", new AnnualTrainingReportDTO("Techno Managerial", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L,
                0L, 0L));

        rowMap.put("Seminar", new AnnualTrainingReportDTO("Seminar/Symposium/Conference/Workshop", 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L,
                0L, 0L));


        for (Requisition req : requisitions) {

            Course course = courseMap.get(req.getCourseId());
            if (course == null) continue;

            CourseType type = courseTypeMap.get(course.getCourseTypeId());
            if (type == null) continue;

            String courseType = type.getCourseType();
            String level = course.getCourseLevel();

            AnnualTrainingReportDTO dto = null;

            // Decide row
            if ("CEP".equalsIgnoreCase(courseType)) {
                dto = rowMap.get("CEP");
            } else if ("Special".equalsIgnoreCase(courseType)) {
                dto = rowMap.get("Special");
            } else if ("Targeted".equalsIgnoreCase(courseType)) {
                dto = rowMap.get("Targeted");
            } else if ("Techno Managerial".equalsIgnoreCase(courseType)) {
                dto = rowMap.get("Techno Managerial");
            } else if ("Training".equalsIgnoreCase(courseType)
                    && "International".equalsIgnoreCase(level)) {
                dto = rowMap.get("Foreign");
            } else if (Stream.of("Seminar", "Symposium", "Conference", "Workshop").anyMatch(courseType::equalsIgnoreCase)) {
                dto = rowMap.get("Seminar");
            }

            if (dto == null) continue;

            EmployeeDTO emp = employeeMap.get(req.getInitiatingOfficer());
            if (emp == null) continue;

            String cadre = emp.getDesigCadre();

            // Cadre classification
            if ("DRDS".equalsIgnoreCase(cadre)) {
                dto.setDrdsTotal(dto.getDrdsTotal() + 1);
            } else if ("DRTC".equalsIgnoreCase(cadre)) {
                dto.setDrtcTotal(dto.getDrtcTotal() + 1);
            } else if ("Admin&Allied".equalsIgnoreCase(cadre)) {
                dto.setAdminAlliedTotal(dto.getAdminAlliedTotal() + 1);
            } else {
                dto.setOthersTotal(dto.getOthersTotal() + 1);
            }
        }

        return new ArrayList<>(rowMap.values());
    }


    public List<BudgetExpenditureDTO> getBudgetExpenditureReport(String username) {
        log.info("Fetching budget expenditure report data");

        List<Requisition> requisitions = requisitionRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<CourseType> courseTypes = courseTypeRepository.findAll();
        List<Sponsorship> sponsorships = sponsorshipRepository.findAll();

        // Convert to Map for fast lookup
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));

        Map<Long, CourseType> courseTypeMap = courseTypes.stream()
                .collect(Collectors.toMap(CourseType::getCourseTypeId, Function.identity()));

        Map<Long, List<Sponsorship>> sponsorshipMap = sponsorships.stream()
                .collect(Collectors.groupingBy(Sponsorship::getEmpId));

        // Group requisitions by empId
        Map<Long, List<Requisition>> requisitionByEmp =
                requisitions.stream().collect(Collectors.groupingBy(Requisition::getInitiatingOfficer));

        List<BudgetExpenditureDTO> result = new ArrayList<>();
        Map<Long, EmployeeDTO> employeeMap = masterCacheService.getLongEmployeeDTOMap();

        for (Map.Entry<Long, List<Requisition>> entry : requisitionByEmp.entrySet()) {

            Long empId = entry.getKey();
            List<Requisition> empRequisitions = entry.getValue();

            BudgetExpenditureDTO dto = new BudgetExpenditureDTO();

            EmployeeDTO employeeDTO = employeeMap.get(empId);
            dto.setEmpId(empId);
            dto.setEmpName(employeeDTO != null ? CommonUtil.buildEmployeeName(employeeDTO, true) : "");

            // Initialize all BigDecimals as ZERO
            dto.setCep(BigDecimal.ZERO);
            dto.setSpecialRE(BigDecimal.ZERO);
            dto.setSpecialFE(BigDecimal.ZERO);
            dto.setTargetedRE(BigDecimal.ZERO);
            dto.setTargetedFE(BigDecimal.ZERO);
            dto.setMeRt(BigDecimal.ZERO);
            dto.setMeDirector(BigDecimal.ZERO);
            dto.setTechManagerial(BigDecimal.ZERO);
            dto.setForeignRE(BigDecimal.ZERO);
            dto.setForeignFE(BigDecimal.ZERO);
            dto.setPhd(BigDecimal.ZERO);
            dto.setRegistrationRE(BigDecimal.ZERO);
            dto.setRegistrationFE(BigDecimal.ZERO);
            dto.setCourseFee(BigDecimal.ZERO);
            dto.setOthersRE(BigDecimal.ZERO);
            dto.setOthersFE(BigDecimal.ZERO);

            for (Requisition req : empRequisitions) {

                Course course = courseMap.get(req.getCourseId());
                if (course == null) continue;

                CourseType courseType = courseTypeMap.get(course.getCourseTypeId());
                if (courseType == null) continue;

                BigDecimal regFee =
                        Optional.ofNullable(req.getRegistrationFee()).orElse(BigDecimal.ZERO);

                String type = courseType.getCourseType();
                String level = course.getCourseLevel();

                // 1. CEP
                if ("CEP".equalsIgnoreCase(type)) {
                    dto.setCep(dto.getCep().add(regFee));
                }

                // 2. Special
                else if ("Special".equalsIgnoreCase(type)) {
                    if ("National".equalsIgnoreCase(level)) {
                        dto.setSpecialRE(dto.getSpecialRE().add(regFee));
                    } else {
                        dto.setSpecialFE(dto.getSpecialFE().add(regFee));
                    }
                }

                // 4. Techno Managerial
                else if ("Techno Managerial".equalsIgnoreCase(type)) {
                    dto.setTechManagerial(dto.getTechManagerial().add(regFee));
                }

                // 5. Training
                else if ("Training".equalsIgnoreCase(type)) {
                    if ("National".equalsIgnoreCase(level)) {
                        dto.setForeignRE(dto.getForeignRE().add(regFee));
                    } else {
                        dto.setForeignFE(dto.getForeignFE().add(regFee));
                    }
                }

                // 7. Seminar/Symposia/Conference/Workshop
                else if (Stream.of("Seminar", "Symposia", "Conference", "Workshop").anyMatch(type::equalsIgnoreCase)) {

                    if ("National".equalsIgnoreCase(level)) {
                        dto.setRegistrationRE(dto.getRegistrationRE().add(regFee));
                    } else {
                        dto.setRegistrationFE(dto.getRegistrationFE().add(regFee));
                    }
                }

                // 8. Course Fee (offlineRegistrationFee)
                if (course.getOfflineRegistrationFee() != null) {
                    dto.setCourseFee(dto.getCourseFee()
                            .add(course.getOfflineRegistrationFee()));
                }
            }

            // 3 & 6 Sponsorship Logic
            List<Sponsorship> empSponsorships =
                    sponsorshipMap.getOrDefault(empId, Collections.emptyList());

            for (Sponsorship sp : empSponsorships) {

                BigDecimal exp = Optional.ofNullable(sp.getExpenditure())
                        .orElse(BigDecimal.ZERO);

                if ("MTECH".equalsIgnoreCase(sp.getDegreeType())) {
                    dto.setMeRt(dto.getMeRt().add(exp));
                    dto.setMeDirector(BigDecimal.ZERO);
                }

                if ("PHD".equalsIgnoreCase(sp.getDegreeType())) {
                    dto.setPhd(dto.getPhd().add(exp));
                }
            }

            // 9 Others already ZERO

            // 10 Total Calculation
            BigDecimal total = Stream.of(
                    dto.getCep(),
                    dto.getSpecialRE(),
                    dto.getSpecialFE(),
                    dto.getTargetedRE(),
                    dto.getTargetedFE(),
                    dto.getMeRt(),
                    dto.getMeDirector(),
                    dto.getTechManagerial(),
                    dto.getForeignRE(),
                    dto.getForeignFE(),
                    dto.getPhd(),
                    dto.getRegistrationRE(),
                    dto.getRegistrationFE(),
                    dto.getCourseFee(),
                    dto.getOthersRE(),
                    dto.getOthersFE()
            ).reduce(BigDecimal.ZERO, BigDecimal::add);

            dto.setTotal(total);

            result.add(dto);
        }

        return result;
    }

    public List<GenderBudgetDTO> getGenderBudgetReport(String username) {

        log.info("Fetching gender budget report data");

        List<Requisition> requisitions = requisitionRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<CourseType> courseTypes = courseTypeRepository.findAll();
        List<Sponsorship> sponsorships = sponsorshipRepository.findAll();

        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));

        Map<Long, CourseType> courseTypeMap = courseTypes.stream()
                .collect(Collectors.toMap(CourseType::getCourseTypeId, Function.identity()));

        long sponsorEmpCount = sponsorships.stream().mapToLong(Sponsorship::getEmpId).count();

        GenderBudgetDTO dto = new GenderBudgetDTO();
        dto.setLabName(labCode);

        initializeZero(dto);

        Set<Long> cepEmp = new HashSet<>();
        Set<Long> specialEmp = new HashSet<>();
        Set<Long> seminarEmp = new HashSet<>();
        Set<Long> foreignEmp = new HashSet<>();

        for (Requisition req : requisitions) {

            Course course = courseMap.get(req.getCourseId());
            if (course == null) continue;

            CourseType type = courseTypeMap.get(course.getCourseTypeId());
            if (type == null) continue;

            String courseType = type.getCourseType();
            String level = course.getCourseLevel();
            Long empId = req.getInitiatingOfficer();

            if ("CEP".equalsIgnoreCase(courseType)) {
                cepEmp.add(empId);
            } else if ("Special".equalsIgnoreCase(courseType)) {
                specialEmp.add(empId);
            } else if (Stream.of("Seminar", "Symposia", "Conference", "Workshop")
                    .anyMatch(courseType::equalsIgnoreCase)) {
                seminarEmp.add(empId);
            } else if ("Training".equalsIgnoreCase(courseType)
                    && "International".equalsIgnoreCase(level)) {
                foreignEmp.add(empId);
            }
        }

        dto.setHigherDegreeTotalNo(BigDecimal.valueOf(sponsorEmpCount));
        dto.setCepTotalNo(BigDecimal.valueOf(cepEmp.size()));
        dto.setSpecialTotalNo(BigDecimal.valueOf(specialEmp.size()));
        dto.setSeminarTotalNo(BigDecimal.valueOf(seminarEmp.size()));
        dto.setForeignTotalNo(BigDecimal.valueOf(foreignEmp.size()));

        // Return as list
        return Collections.singletonList(dto);
    }

    public List<TrainingSCSTDTO> getTrainingSCSTReport(String token, String username) {

        List<EmployeeDTO> employeeDTOList = getNominalRollList(token);
        List<Requisition> requisitions = requisitionRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<CourseType> courseTypes = courseTypeRepository.findAll();

        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));

        Map<Long, CourseType> courseTypeMap = courseTypes.stream()
                .collect(Collectors.toMap(CourseType::getCourseTypeId, Function.identity()));

        // Convert employee list to map for fast lookup
        Map<Long, EmployeeDTO> employeeMap = employeeDTOList.stream()
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, Function.identity()));

        TrainingSCSTDTO dto = new TrainingSCSTDTO();
        dto.setLabName(labCode);

        // Initialize all numeric fields to 0
        initializeTrainingDtoZero(dto);

        // Use Set to avoid duplicate employee counting
        Set<Long> cepSet = new HashSet<>();
        Set<Long> specialSet = new HashSet<>();
        Set<Long> seminarSet = new HashSet<>();
        Set<Long> foreignSet = new HashSet<>();

        for (Requisition req : requisitions) {

            EmployeeDTO emp = employeeMap.get(req.getInitiatingOfficer());
            if (emp == null || emp.getEmployeeDetails() == null) continue;

            String category = emp.getEmployeeDetails().getCategory();
            if (category == null) continue;

            // Only SC / ST
            if (!("SC".equalsIgnoreCase(category) || "ST".equalsIgnoreCase(category)))
                continue;

            Course course = courseMap.get(req.getCourseId());
            if (course == null) continue;

            CourseType type = courseTypeMap.get(course.getCourseTypeId());
            if (type == null) continue;

            String courseType = type.getCourseType();
            String level = course.getCourseLevel();
            Long empId = req.getInitiatingOfficer();

            // 1. CEP
            if ("CEP".equalsIgnoreCase(courseType)) {
                cepSet.add(empId);
            }

            // 1. Special
            else if ("Special".equalsIgnoreCase(courseType)) {
                specialSet.add(empId);
            }

            // 2. Seminar/Symposia/Conference/Workshop
            else if (Stream.of("Seminar", "Symposia", "Conference", "Workshop").anyMatch(courseType::equalsIgnoreCase)) {

                seminarSet.add(empId);
            }

            // 3. Training + International
            else if ("Training".equalsIgnoreCase(courseType)
                    && "International".equalsIgnoreCase(level)) {

                foreignSet.add(empId);
            }
        }

        // Set counts
        dto.setCepScstTrained((long) cepSet.size());
        dto.setSpecialTargetedScstTrained((long) specialSet.size());
        dto.setSponsoredSeminarScstTrained((long) seminarSet.size());
        dto.setForeignTrainingScstTrained((long) foreignSet.size());

        return Collections.singletonList(dto);
    }

    private void initializeTrainingDtoZero(TrainingSCSTDTO dto) {

        dto.setCepScstTrained(0L);
        dto.setCepExpenditure(BigDecimal.ZERO);
        dto.setCepPercentage(0L);

        dto.setSpecialTargetedScstTrained(0L);
        dto.setSpecialTargetedExpenditure(BigDecimal.ZERO);
        dto.setSpecialTargetedPercentage(0L);

        dto.setHigherDegreeScstTrained(0L);
        dto.setHigherDegreeExpenditure(BigDecimal.ZERO);
        dto.setHigherDegreePercentage(0L);

        dto.setSponsoredSeminarScstTrained(0L);
        dto.setSponsoredSeminarExpenditure(BigDecimal.ZERO);
        dto.setSponsoredSeminarPercentage(0L);

        dto.setForeignTrainingScstTrained(0L);
        dto.setForeignTrainingExpenditure(0L);
        dto.setForeignTrainingPercentage(0L);

        dto.setOthersScstTrained(0L);
        dto.setOthersExpenditure(0L);
        dto.setOthersPercentage(0L);
    }

    private void initializeZero(GenderBudgetDTO dto) {

        BigDecimal zero = BigDecimal.ZERO;

        dto.setCepMaleNo(zero);
        dto.setCepMaleExp(zero);
        dto.setCepFemaleNo(zero);
        dto.setCepFemaleExp(zero);
        dto.setCepTotalNo(zero);
        dto.setCepTotalExp(zero);

        dto.setSpecialMaleNo(zero);
        dto.setSpecialMaleExp(zero);
        dto.setSpecialFemaleNo(zero);
        dto.setSpecialFemaleExp(zero);
        dto.setSpecialTotalNo(zero);
        dto.setSpecialTotalExp(zero);

        dto.setHigherDegreeMaleNo(zero);
        dto.setHigherDegreeMaleExp(zero);
        dto.setHigherDegreeFemaleNo(zero);
        dto.setHigherDegreeFemaleExp(zero);
        dto.setHigherDegreeTotalNo(zero);
        dto.setHigherDegreeTotalExp(zero);

        dto.setSeminarMaleNo(zero);
        dto.setSeminarMaleExp(zero);
        dto.setSeminarFemaleNo(zero);
        dto.setSeminarFemaleExp(zero);
        dto.setSeminarTotalNo(zero);
        dto.setSeminarTotalExp(zero);

        dto.setForeignMaleNo(zero);
        dto.setForeignMaleExp(zero);
        dto.setForeignFemaleNo(zero);
        dto.setForeignFemaleExp(zero);
        dto.setForeignTotalNo(zero);
        dto.setForeignTotalExp(zero);

        dto.setOthersMaleNo(zero);
        dto.setOthersMaleExp(zero);
        dto.setOthersFemaleNo(zero);
        dto.setOthersFemaleExp(zero);
        dto.setOthersTotalNo(zero);
        dto.setOthersTotalExp(zero);
    }

}
