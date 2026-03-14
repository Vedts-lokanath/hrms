package com.vts.hrms.service;

import com.vts.hrms.dto.*;
import com.vts.hrms.entity.*;
import com.vts.hrms.entity.Calendar;
import com.vts.hrms.entity.Course;
import com.vts.hrms.entity.Requisition;
import com.vts.hrms.exception.NotFoundException;
import com.vts.hrms.mapper.*;
import com.vts.hrms.repository.*;
import com.vts.hrms.util.FileStorageUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    @Value("${appStorage}")
    private String appStorage;

    @Value("${x_api_key}")
    private String xApiKey;

    @Value("${labCode}")
    private String labCode;

    private final MasterClientService masterClient;
    private final OrganizerRepository organizerRepository;
    private final OrganizerMapper organizerMapper;
    private final CalendarMapper calenderMapper;
    private final CalenderRepository calenderRepository;
    private final CourseMapper courseMapper;
    private final CourseRepository courseRepository;
    private final RequisitionMapper requisitionMapper;
    private final RequisitionRepository requisitionRepository;
    private final FeedbackMapper feedbackMapper;
    private final FeedbackRepository feedbackRepository;
    private final RequisitionTransactionRepository transactionRepository;
    private final StatusRepository statusRepository;
    private final NotificationRepository notificationRepository;
    private final SignRoleAuthorityRepository signRoleAuthorityRepository;
    private final RequisitionSequenceRepository sequenceRepository;
    private final EvaluationRepository evaluationRepository;
    private final EligibilityMapper eligibilityMapper;
    private final EligibilityRepository eligibilityRepository;


    public TrainingService(MasterClientService masterClient, OrganizerRepository organizerRepository, OrganizerMapper organizerMapper, CalendarMapper calenderMapper, CalenderRepository calenderRepository, CourseMapper courseMapper, CourseRepository courseRepository, RequisitionMapper requisitionMapper, RequisitionRepository requisitionRepository, FeedbackMapper feedbackMapper, FeedbackRepository feedbackRepository, RequisitionTransactionRepository transactionRepository, StatusRepository statusRepository, NotificationRepository notificationRepository, SignRoleAuthorityRepository signRoleAuthorityRepository, RequisitionSequenceRepository sequenceRepository, EvaluationRepository evaluationRepository, EligibilityMapper eligibilityMapper, EligibilityRepository eligibilityRepository) {
        this.masterClient = masterClient;
        this.organizerRepository = organizerRepository;
        this.organizerMapper = organizerMapper;
        this.calenderMapper = calenderMapper;
        this.calenderRepository = calenderRepository;
        this.courseMapper = courseMapper;
        this.courseRepository = courseRepository;
        this.requisitionMapper = requisitionMapper;
        this.requisitionRepository = requisitionRepository;
        this.feedbackMapper = feedbackMapper;
        this.feedbackRepository = feedbackRepository;
        this.transactionRepository = transactionRepository;
        this.statusRepository = statusRepository;
        this.notificationRepository = notificationRepository;
        this.signRoleAuthorityRepository = signRoleAuthorityRepository;
        this.sequenceRepository = sequenceRepository;
        this.evaluationRepository = evaluationRepository;
        this.eligibilityMapper = eligibilityMapper;
        this.eligibilityRepository = eligibilityRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizerDTO> getAllAgencies() {
        List<Organizer> list = organizerRepository.findAllByIsActive(1);
        list = list.stream().sorted(Comparator.comparing(Organizer::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()))).toList();
        return list.stream().map(organizerMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Transactional
    public CalendarDTO addCalenderData(CalendarDTO dto, String username) throws IOException {
        log.info("Request to save calender for year {} by {}", dto.getYear(), username);
        Calendar calender = calenderMapper.toEntity(dto);
        calender.setCreatedBy(username);
        calender.setCreatedDate(LocalDateTime.now());
        calender.setIsActive(1);

        OrganizerDTO organizerDTO = getOrganizerById(dto.getOrganizerId())
                .orElseThrow(() -> new NotFoundException("Organizer data not found"));

        Path fullpath = Paths.get(appStorage, "Calendar", dto.getYear(), organizerDTO.getOrganizer().trim());

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            calender.setCalendarFileName(dto.getFile().getOriginalFilename());
            FileStorageUtil.saveFile(fullpath, dto.getFile().getOriginalFilename(), dto.getFile());
        }

        if (dto.getCoverFile() != null && !dto.getCoverFile().isEmpty()) {
            calender.setCoveringLetter(dto.getCoverFile().getOriginalFilename());
            FileStorageUtil.saveFile(fullpath, dto.getCoverFile().getOriginalFilename(), dto.getCoverFile());
        }

        calender = calenderRepository.save(calender);
        return calenderMapper.toDto(calender);
    }

    @Transactional(readOnly = true)
    public List<CalendarDTO> getCalenderList(String year, String username) {
        log.info("Calender list fetched by {}", username);
        List<Organizer> agencies = organizerRepository.findAllByIsActive(1);
        List<Calendar> calenderList = calenderRepository.findAllByYearAndIsActive(year, 1);

        Map<Long, Organizer> agencyMap = agencies.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        List<CalendarDTO> dtoList = calenderMapper.toDto(calenderList);

        dtoList.forEach(dto -> {
            Organizer organizer = agencyMap.get(dto.getOrganizerId());
            if (organizer != null) {
                dto.setOrganizer(organizer.getOrganizer());
            }
        });
        return dtoList;
    }

    public Optional<CalendarDTO> getCalendarById(Long id, String username) {
        return calenderRepository.findById(id).map(calenderMapper::toDto);
    }

    @Transactional
    public CourseDTO addCourseData(@Valid CourseDTO dto, String username) {
        log.info("Request to add course {} by {}", dto.getCourseName(), username);
        Course course = courseMapper.toEntity(dto);
        course.setCreatedBy(username);
        course.setCreatedDate(LocalDateTime.now());
        course.setIsActive(1);
        course = courseRepository.save(course);

        Organizer organizer = organizerRepository.findById(course.getOrganizerId())
                .orElseThrow(() -> new NotFoundException("Organizer data not found"));

        CourseDTO courseDTO = courseMapper.toDto(course);
        courseDTO.setOrganizer(organizer.getOrganizer());

        return courseDTO;
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getCourseList(Long orgId, String username) {
        log.info("Course list fetched for organizer id {} by {}", orgId, username);

        if (orgId == null) {
            return List.of();
        }
        List<Course> courseList = new ArrayList<>();
        List<Organizer> organizerList = organizerRepository.findAllByIsActive(1);
        if (orgId > 0) {
            courseList = courseRepository.findAllByOrganizerIdAndIsActive(orgId, 1);
        } else {
            courseList = courseRepository.findAllByIsActive(1);
        }
        List<Eligibility> eligibilityList = eligibilityRepository.findAllByIsActive(1);

        courseList = courseList.stream()
                .sorted(Comparator.comparing(Course::getCreatedDate).reversed())
                .toList();

        Map<Long, Organizer> organizerMap = organizerList.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        Map<Long, Eligibility> eligibilityMap = eligibilityList.stream()
                .collect(Collectors.toMap(Eligibility::getEligibilityId, Function.identity()));

        List<CourseDTO> dtoList = courseMapper.toDto(courseList);

        dtoList.forEach(dto -> {
            Organizer organizer = organizerMap.get(dto.getOrganizerId());
            Eligibility eligibility = eligibilityMap.get(dto.getEligibilityId());

            if (organizer != null) {
                dto.setOrganizer(organizer.getOrganizer());
            }
            if (eligibility != null) {
                dto.setEligibilityName(eligibility.getEligibilityName());
            }
        });
        return dtoList;
    }

    @Transactional
    public RequisitionDTO addRequisitionData(@Valid RequisitionDTO dto, String username) throws IOException {
        log.info("Request to add requisition for program {} by {}", dto.getCourseName(), username);

        Requisition requisition = requisitionMapper.toEntity(dto);
        requisition.setStatus("AA");
        requisition.setCreatedBy(username);
        requisition.setCreatedDate(LocalDateTime.now());
        requisition.setIsActive(1);

        LocalDate fromDate = requisition.getFromDate();

        if (fromDate == null) {
            throw new IllegalArgumentException("From Date is required");
        }

        String fy = getFinancialYear(fromDate);

        RequisitionSequence sequence =
                sequenceRepository.findByFinancialYearForUpdate(fy)
                        .orElseGet(() -> {
                            RequisitionSequence newSeq = new RequisitionSequence();
                            newSeq.setFinancialYear(fy);
                            newSeq.setLastNumber(0L);
                            return newSeq;
                        });

        Long nextNumber = sequence.getLastNumber() + 1;
        sequence.setLastNumber(nextNumber);
        sequenceRepository.save(sequence);

        String requisitionNumber = "REQ/" + fy + "/" + String.format("%03d", nextNumber);
        requisition.setRequisitionNumber(requisitionNumber);

        Path fullpath = Paths.get(appStorage, "Requisition", requisitionNumber.replace("/", "_"));
        if (dto.getMultipartFileEcs() != null && !dto.getMultipartFileEcs().isEmpty()) {
            requisition.setFileEcs(dto.getMultipartFileEcs().getOriginalFilename());
            FileStorageUtil.saveFile(fullpath, dto.getMultipartFileEcs().getOriginalFilename(), dto.getMultipartFileEcs());
        }
        if (dto.getMultipartFileCheque() != null && !dto.getMultipartFileCheque().isEmpty()) {
            requisition.setFileCheque(dto.getMultipartFileCheque().getOriginalFilename());
            FileStorageUtil.saveFile(fullpath, dto.getMultipartFileCheque().getOriginalFilename(), dto.getMultipartFileCheque());
        }
        if (dto.getMultipartFilePan() != null && !dto.getMultipartFilePan().isEmpty()) {
            requisition.setFilePan(dto.getMultipartFilePan().getOriginalFilename());
            FileStorageUtil.saveFile(fullpath, dto.getMultipartFilePan().getOriginalFilename(), dto.getMultipartFilePan());
        }
        if (dto.getMultipartFileBrochure() != null && !dto.getMultipartFileBrochure().isEmpty()) {
            requisition.setFileBrochure(dto.getMultipartFileBrochure().getOriginalFilename());
            FileStorageUtil.saveFile(fullpath, dto.getMultipartFileBrochure().getOriginalFilename(), dto.getMultipartFileBrochure());
        }

        requisition = requisitionRepository.save(requisition);
        insertTransaction(requisition.getRequisitionId(), requisition.getInitiatingOfficer(), requisition.getInitiatingOfficer(), username, "AA", null);
        return requisitionMapper.toDto(requisition);
    }

    @Transactional(readOnly = true)
    public List<RequisitionDTO> getRequisitionList(Long empId, String roleName, String username) {
        log.info("Requisition list fetched for role {} by {}", roleName, username);
        List<Organizer> organizerList = organizerRepository.findAllByIsActive(1);
        List<Course> courseList = courseRepository.findAllByIsActive(1);
        List<Status> statusList = statusRepository.findAll();

        List<Requisition> list = new ArrayList<>();

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

        if (Arrays.asList("ROLE_ADMIN", "ROLE_AD_HRT").contains(roleName)) {

            list = requisitionRepository
                    .findAllByIsActiveOrderByRequisitionIdDesc(1);

        } else if ("ROLE_DH".equalsIgnoreCase(roleName)) {

            List<DivisionDTO> divisionList = masterClient.getDivisionMaster(xApiKey);

            Optional<DivisionDTO> divisionOpt = divisionList.stream()
                    .filter(d -> Objects.equals(d.getDivisionHeadId(), empId))
                    .findFirst();

            if (divisionOpt.isPresent()) {

                Long divisionId = divisionOpt.get().getDivisionId();
                List<Long> empIds = employeeList.stream()
                        .filter(e -> labCode != null && labCode.equalsIgnoreCase(e.getLabCode()))
                        .filter(emp -> Objects.equals(emp.getDivisionId(), divisionId))
                        .map(EmployeeDTO::getEmpId)
                        .collect(Collectors.toList());

                list = requisitionRepository
                        .findAllByInitiatingOfficerInAndIsActiveOrderByRequisitionIdDesc(empIds, 1);

            } else {
                list = new ArrayList<>();
            }

        } else {

            list = requisitionRepository
                    .findAllByInitiatingOfficerAndIsActiveOrderByRequisitionIdDesc(empId, 1);
        }

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
                dto.setEmpDesigName(employeeDTO.getEmpDesigName());
                dto.setEmpDivCode(employeeDTO.getEmpDivCode());
                dto.setEmail(employeeDTO.getEmail());
                dto.setMobileNo(employeeDTO.getMobileNo());
            }
        });
        return dtoList;
    }

    @Transactional
    public RequisitionDTO getRequisitionById(Long id, String username) {
        log.info("Request to fetch Requisition data for id {} by {}", id, username);
        if (id == null) {
            throw new NotFoundException("Requisition id cannot be null");
        }
        List<Organizer> organizerList = organizerRepository.findAllByIsActive(1);
        List<Course> courseList = courseRepository.findAllByIsActive(1);

        Map<Long, Organizer> organizerMap = organizerList.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        Map<Long, Course> courseMap = courseList.stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));

        Requisition requisition = requisitionRepository.findById(id).orElseThrow(() -> new NotFoundException("Requisition not found"));

        Course course = courseMap.get(requisition.getCourseId());
        Organizer org = organizerMap.get(course.getOrganizerId());

        RequisitionDTO requisitionDTO = requisitionMapper.toDto(requisition);
        requisitionDTO.setOrganizer(org.getOrganizer());
        requisitionDTO.setOrganizerId(org.getOrganizerId());
        requisitionDTO.setCourseName(course.getCourseName());
        requisitionDTO.setVenue(course.getVenue());
        requisitionDTO.setOfflineRegistrationFee(course.getOfflineRegistrationFee());
        requisitionDTO.setOnlineRegistrationFee(course.getOnlineRegistrationFee());
        return requisitionDTO;
    }

    @Transactional
    public Optional<RequisitionDTO> updateRequisitionData(@Valid RequisitionDTO dto, String username) {

        log.info("Request to update requisition for id {} by {}", dto.getRequisitionId(), username);

        Path fullpath = Paths.get(appStorage, "Requisition");

        return requisitionRepository
                .findById(dto.getRequisitionId())
                .map(existingReq -> {
                    existingReq.setModifiedBy(username);
                    existingReq.setModifiedDate(LocalDateTime.now());

                    // ECS
                    updateFile(dto.getMultipartFileEcs(), existingReq.getFileEcs(), fullpath, existingReq::setFileEcs);
                    // Cheque
                    updateFile(dto.getMultipartFileCheque(), existingReq.getFileCheque(), fullpath, existingReq::setFileCheque);
                    // PAN
                    updateFile(dto.getMultipartFilePan(), existingReq.getFilePan(), fullpath, existingReq::setFilePan);
                    // Brochure
                    updateFile(dto.getMultipartFileBrochure(), existingReq.getFileBrochure(), fullpath, existingReq::setFileBrochure);

                    requisitionMapper.partialUpdate(existingReq, dto);
                    return existingReq;
                })
                .map(requisitionRepository::save)
                .map(requisitionMapper::toDto);
    }

    private void updateFile(MultipartFile multipartFile,
                            String existingFileName,
                            Path fullPath,
                            Consumer<String> setFileName) {

        if (multipartFile != null && !multipartFile.isEmpty()) {
            // Delete old file if exists
            if (existingFileName != null) {
                Path oldFilePath = fullPath.resolve(existingFileName);
                try {
                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                        log.info("Deleted old file: {}", oldFilePath);
                    }
                } catch (Exception ex) {
                    log.warn("Failed to delete old file: {}", oldFilePath, ex);
                }
            }
            // Save new file
            String newFileName = multipartFile.getOriginalFilename();
            try {
                FileStorageUtil.saveFile(fullPath, newFileName, multipartFile);
                setFileName.accept(newFileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file", e);
            }
        }
    }


    public FeedbackDTO requisitionFeedback(@Valid FeedbackDTO dto, String username) {
        log.info("Request to requisition feedback requisitionId {} by {}", dto.getRequisitionId(), username);
        Feedback feedback = feedbackMapper.toEntity(dto);
        feedback.setIsAccepted("N");
        feedback.setCreatedBy(username);
        feedback.setCreatedDate(LocalDateTime.now());
        feedback.setIsActive(1);
        feedback = feedbackRepository.save(feedback);
        return feedbackMapper.toDto(feedback);
    }

    public List<FeedbackDTO> getFeedbackList(Long empId, String roleName, String username) {
        log.info("Feedback list fetched by {}", username);

        List<Feedback> feedbackList = new ArrayList<>();
        List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);

        if (Arrays.asList("ROLE_ADMIN", "ROLE_AD_HRT").contains(roleName) && empId == 0) {

            feedbackList = feedbackRepository.findByIsActiveOrderByFeedbackIdDesc(1);

        } else if ("ROLE_DH".equalsIgnoreCase(roleName)) {

            List<DivisionDTO> divisionList = masterClient.getDivisionMaster(xApiKey);

            Optional<DivisionDTO> divisionOpt = divisionList.stream()
                    .filter(d -> Objects.equals(d.getDivisionHeadId(), empId))
                    .findFirst();

            if (divisionOpt.isPresent()) {

                Long divisionId = divisionOpt.get().getDivisionId();
                List<Long> empIds = employeeList.stream()
                        .filter(e -> labCode != null && labCode.equalsIgnoreCase(e.getLabCode()))
                        .filter(emp -> Objects.equals(emp.getDivisionId(), divisionId))
                        .map(EmployeeDTO::getEmpId)
                        .collect(Collectors.toList());

                feedbackList = feedbackRepository
                        .findAllByParticipantIdInAndIsActiveOrderByFeedbackIdDesc(empIds, 1);

            } else {
                feedbackList = new ArrayList<>();
            }

        } else {
            feedbackList = feedbackRepository
                    .findAllByParticipantIdAndIsActiveOrderByFeedbackIdDesc(empId, 1);
        }

        if (feedbackList == null || feedbackList.isEmpty()) {
            return Collections.emptyList();
        }

        List<FeedbackDTO> feedbbackdto = feedbackMapper.toDto(feedbackList);

        if (feedbbackdto == null || feedbbackdto.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, EmployeeDTO> employeeMap = employeeList != null
                ? employeeList.stream()
                .filter(e -> labCode != null && labCode.equalsIgnoreCase(e.getLabCode()))
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp))
                : Collections.emptyMap();

        feedbbackdto.forEach(d -> {

            if (d == null) return;

            EmployeeDTO employeeDTO = employeeMap.get(d.getParticipantId());

            if (employeeDTO != null) {
                d.setParticipantName(buildEmployeeName(employeeDTO, true));
                d.setDivisionName(employeeDTO.getEmpDivCode());
            }

            RequisitionDTO requisitionDto = null;

            if (d.getRequisitionId() != null) {
                requisitionDto = getRequisitionById(d.getRequisitionId(), username);
            }

            if (requisitionDto != null) {
                d.setRequisitionNumber(requisitionDto.getRequisitionNumber());
                d.setCourseName(requisitionDto.getCourseName());
                d.setOrganizer(requisitionDto.getOrganizer());
                d.setFromDate(requisitionDto.getFromDate());
                d.setToDate(requisitionDto.getToDate());
                d.setProgramDuration(requisitionDto.getDuration());
            }

        });

        return feedbbackdto;
    }

    public Optional<CourseDTO> editCourseData(@Valid CourseDTO dto, String username) {
        log.info("Request to edit program id {} by {}", dto.getCourseId(), username);

        return courseRepository
                .findById(dto.getCourseId())
                .map(existingCourse -> {
                    existingCourse.setModifiedBy(username);
                    existingCourse.setModifiedDate(LocalDateTime.now());
                    courseMapper.partialUpdate(existingCourse, dto);
                    return existingCourse;
                })
                .map(courseRepository::save)
                .map(courseMapper::toDto);
    }

    public OrganizerDTO addOrganizer(@Valid OrganizerDTO dto, String username) {
        log.info("Request to add organizer {} by {}", dto.getOrganizer(), username);
        Organizer organizer = organizerMapper.toEntity(dto);
        organizer.setCreatedBy(username);
        organizer.setCreatedDate(LocalDateTime.now());
        organizer.setIsActive(1);
        organizer = organizerRepository.save(organizer);

        return organizerMapper.toDto(organizer);
    }

    public Optional<OrganizerDTO> editOrganizer(@Valid OrganizerDTO dto, String username) {
        log.info("Request to edit organizer {} by {}", dto.getOrganizer(), username);

        return organizerRepository
                .findById(dto.getOrganizerId())
                .map(organizer -> {
                    organizer.setModifiedBy(username);
                    organizer.setModifiedDate(LocalDateTime.now());
                    organizerMapper.partialUpdate(organizer, dto);
                    return organizer;
                })
                .map(organizerRepository::save)
                .map(organizerMapper::toDto);
    }

    @CacheEvict(value = "notificationList", allEntries = true)
    @Transactional
    public RequisitionDTO forwardRequisition(@Valid RequisitionDTO dto, String username) {
        log.info("Request to froward requisition for id {} ", dto.getRequisitionId());

        if (dto.getRequisitionId() == null) {
            throw new NotFoundException("Requisition Id can not be null");
        }

        Requisition requisition = requisitionRepository.findById(dto.getRequisitionId())
                .orElseThrow(() -> new NotFoundException("Requisition not found"));

        requisition.setStatus("AF");
        requisition.setModifiedBy(username);
        requisition.setModifiedDate(LocalDateTime.now());

        List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);

        Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
                .filter(e -> labCode != null && labCode.equalsIgnoreCase(e.getLabCode()))
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

        EmployeeDTO employeeDTO = employeeMap.get(requisition.getInitiatingOfficer());

        String message = getNotificationMsg(requisition.getRequisitionNumber(), employeeDTO, "Forward by");

        DivisionDTO divisionDTO = Optional.of(employeeDTO)
                .map(EmployeeDTO::getDivisionId)
                .flatMap(id -> Optional.ofNullable(masterClient.getDivisionMaster(xApiKey))
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(d -> id.equals(d.getDivisionId()))
                        .findFirst())
                .orElseThrow(() -> new NotFoundException("Division data not found"));

        insertTransaction(dto.getRequisitionId(), dto.getActionBy(), divisionDTO.getDivisionHeadId(), username, "AF", null);
        insertNotification(dto.getActionBy(), divisionDTO.getDivisionHeadId(), "req-approval", message, username);
        return requisitionMapper.toDto(requisition);
    }

    @CacheEvict(value = "notificationList", allEntries = true)
    @Transactional
    public RequisitionDTO recommendRequisition(@Valid RequisitionDTO dto, String username) {
        log.info("Request to recommend requisition for id {} ", dto.getRequisitionId());

        if (dto.getRequisitionId() == null) {
            throw new NotFoundException("Requisition Id can not be null");
        }

        Requisition requisition = requisitionRepository.findById(dto.getRequisitionId())
                .orElseThrow(() -> new NotFoundException("Requisition not found"));

        if (requisition.getStatus().equalsIgnoreCase("AR")) {
            requisition.setStatus("AV");
            insertTransaction(dto.getRequisitionId(), dto.getActionBy(), dto.getActionBy(), username, "AV", null);
        } else {
            requisition.setStatus("AR");

            SignRoleAuthorityDTO authorityDTO = signRoleAuthorityRepository.findBySignAuthRole("AD-HRT");
            if (authorityDTO.getSignRoleAuthorityId() == null) {
                throw new NotFoundException("In SignRoleAuthority AD-HRT role not found");
            }

            List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);

            Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
                    .filter(e -> labCode != null && labCode.equalsIgnoreCase(e.getLabCode()))
                    .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

            EmployeeDTO employeeDTO = employeeMap.get(dto.getActionBy());

            String message = getNotificationMsg(requisition.getRequisitionNumber(), employeeDTO, "Forward by");
            insertTransaction(dto.getRequisitionId(), dto.getActionBy(), authorityDTO.getEmpId(), username, "AR", null);
            insertNotification(dto.getActionBy(), authorityDTO.getEmpId(), "req-approval", message, username);
        }
        requisition.setModifiedBy(username);
        requisition.setModifiedDate(LocalDateTime.now());
        return requisitionMapper.toDto(requisition);
    }

    private static String getNotificationMsg(String requisitionNumber, EmployeeDTO employeeDTO, String messageName) {
        String prefix = employeeDTO.getTitle() != null && !employeeDTO.getTitle().trim().isEmpty()
                ? employeeDTO.getTitle()
                : (employeeDTO.getSalutation() != null ? employeeDTO.getSalutation() : "");

        return String.format(
                "Requisition no " + requisitionNumber + " " + messageName + " " + "%s%s%s",
                prefix.isEmpty() ? "" : prefix + " ",
                employeeDTO.getEmpName() != null ? employeeDTO.getEmpName() : "",
                employeeDTO.getEmpDesigName() != null ? ", " + employeeDTO.getEmpDesigName() : ""
        );
    }

    @Transactional(readOnly = true)
    public RequisitionDTO getRequisitionPrint(Long id, String username) {

        log.info("Request to fetch Requisition print data for id {} by {}", id, username);

        if (id == null) {
            throw new NotFoundException("Requisition id cannot be null");
        }

        // Fetch requisition
        Requisition requisition = requisitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Requisition not found"));

        RequisitionDTO dto = requisitionMapper.toDto(requisition);

        // Fetch master data
        List<Organizer> organizerList = organizerRepository.findAllByIsActive(1);
        List<Course> courseList = courseRepository.findAllByIsActive(1);
        List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);

        // Convert to maps safely
        Map<Long, Organizer> organizerMap = organizerList.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        Map<Long, Course> courseMap = courseList.stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));

        Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
                .filter(e -> labCode != null && labCode.equalsIgnoreCase(e.getLabCode()))
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, Function.identity()));

        // Fetch latest transactions
        List<RequisitionTransaction> transactions =
                transactionRepository.findByRequisitionIdAndIsActiveOrderByActionDateDesc(
                        requisition.getRequisitionId(), 1);

        Map<String, RequisitionTransaction> transactionMap =
                Optional.ofNullable(transactions)
                        .orElse(Collections.emptyList())
                        .stream()
                        .collect(Collectors.toMap(
                                RequisitionTransaction::getStatusCode,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

        // =========================
        // Course & Organizer
        // =========================
        Course course = courseMap.get(requisition.getCourseId());

        if (course != null) {
            dto.setCourseName(course.getCourseName());
            dto.setVenue(course.getVenue());
            dto.setOfflineRegistrationFee(course.getOfflineRegistrationFee());
            dto.setOnlineRegistrationFee(course.getOnlineRegistrationFee());

            Organizer organizer = organizerMap.get(course.getOrganizerId());
            if (organizer != null) {
                dto.setOrganizerId(organizer.getOrganizerId());
                dto.setOrganizer(organizer.getOrganizer());
                dto.setOrganizerContactName(organizer.getContactName());
                dto.setOrganizerPhoneNo(organizer.getPhoneNo());
                dto.setOrganizerFaxNo(organizer.getFaxNo());
                dto.setOrganizerEmail(organizer.getEmail());
            }
        }

        // =========================
        // Initiating Officer
        // =========================
        EmployeeDTO initiator = employeeMap.get(requisition.getInitiatingOfficer());

        if (initiator != null) {
            dto.setEmpNo(initiator.getEmpNo());
            dto.setInitiatingOfficerName(
                    buildEmployeeName(initiator, false)
            );
            dto.setEmpDesigName(initiator.getEmpDesigName());
            dto.setEmpDivCode(initiator.getEmpDivCode());
            dto.setEmail(initiator.getEmail());
            dto.setMobileNo(initiator.getMobileNo());
        }

        // =========================
        // Verified & Approved
        // =========================
        RequisitionTransaction forwardTxn = transactionMap.get("AF");
        RequisitionTransaction verifyTxn = transactionMap.get("AR");
        RequisitionTransaction approveTxn = transactionMap.get("AV");

        EmployeeDTO verified = null;
        EmployeeDTO approved = null;

        if (forwardTxn != null) dto.setForwardDate(forwardTxn.getActionDate());

        if (verifyTxn != null && verifyTxn.getActionBy() != null) {
            verified = employeeMap.get(verifyTxn.getActionBy());
            dto.setVerifiedBy(verifyTxn.getActionBy());
            dto.setVerifiedDate(verifyTxn.getActionDate());
        }

        if (approveTxn != null && approveTxn.getActionBy() != null) {
            approved = employeeMap.get(approveTxn.getActionBy());
            dto.setApprovedBy(approveTxn.getActionBy());
            dto.setApprovedDate(approveTxn.getActionDate());
        }

        if (verified != null) {
            dto.setVerifiedOfficerName(
                    buildEmployeeName(verified, true)
            );
        }

        if (approved != null) {
            dto.setApprovedOfficerName(
                    buildEmployeeName(approved, true)
            );
        }

        return dto;
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

    @Transactional(readOnly = true)
    public List<RequisitionDTO> getRequisitionApprovalList(Long empId, String username) {

        log.info("Request to fetch Requisition approval data for empId {} by {}", empId, username);

        if (empId == null) {
            throw new NotFoundException("Employee id cannot be null");
        }

        List<String> statusCodes = List.of("AF", "AR");

        List<Requisition> requisitionList = requisitionRepository.findApprovalList(empId, statusCodes);

        if (requisitionList.isEmpty()) {
            return Collections.emptyList();
        }

        List<RequisitionDTO> dtoList = requisitionMapper.toDto(requisitionList);
        // Fetch master data
        List<Organizer> organizerList = organizerRepository.findAllByIsActive(1);
        List<Course> courseList = courseRepository.findAllByIsActive(1);
        List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);
        List<Status> statusList = statusRepository.findAll();

        Map<String, Status> statusMap = statusList.stream()
                .collect(Collectors.toMap(Status::getStatusCode, Function.identity()));

        Map<Long, Organizer> organizerMap = organizerList.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        Map<Long, Course> courseMap = courseList.stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));

        Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
                .filter(e -> labCode != null && labCode.equalsIgnoreCase(e.getLabCode()))
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, Function.identity()));

        List<RequisitionTransaction> transactions =
                transactionRepository.findAllByActionToAndStatusCodeInAndIsActive(empId, statusCodes, 1);

        // Keep latest transaction per requisition
        Map<Long, RequisitionTransaction> transactionMap =
                transactions.stream()
                        .collect(Collectors.toMap(
                                RequisitionTransaction::getRequisitionId,
                                Function.identity(),
                                (existing, replacement) ->
                                        existing.getActionDate().isAfter(replacement.getActionDate())
                                                ? existing
                                                : replacement
                        ));


        for (RequisitionDTO dto : dtoList) {

            // Course + Organizer
            Course course = courseMap.get(dto.getCourseId());
            if (course != null) {

                dto.setCourseName(course.getCourseName());
                dto.setVenue(course.getVenue());
                dto.setOfflineRegistrationFee(course.getOfflineRegistrationFee());
                dto.setOnlineRegistrationFee(course.getOnlineRegistrationFee());

                Organizer organizer = organizerMap.get(course.getOrganizerId());
                if (organizer != null) {
                    dto.setOrganizerId(organizer.getOrganizerId());
                    dto.setOrganizer(organizer.getOrganizer());
                    dto.setOrganizerContactName(organizer.getContactName());
                    dto.setOrganizerPhoneNo(organizer.getPhoneNo());
                    dto.setOrganizerFaxNo(organizer.getFaxNo());
                    dto.setOrganizerEmail(organizer.getEmail());
                }
            }

            Status status = statusMap.get(dto.getStatus());
            if (status != null) {
                dto.setStatusName(status.getStatusName());
                dto.setStatusColor(status.getColorCode());
            }

            // Transaction Info
            RequisitionTransaction txn = transactionMap.get(dto.getRequisitionId());

            if (txn != null) {
                dto.setForwardDate(txn.getActionDate());
                dto.setActionTo(txn.getActionTo());
                if (txn.getActionBy() != null) {
                    EmployeeDTO forwarded = employeeMap.get(txn.getActionBy());
                    if (forwarded != null) {
                        dto.setForwardByName(buildEmployeeName(forwarded, true));
                    }
                }
            }
        }

        return dtoList;
    }

    @Transactional(readOnly = true)
    public List<RequisitionTransactionDTO> getRequisitionTransaction(Long reqId, String username) {
        log.info("Request to fetch Requisition transaction data for requisitionId {} by {}", reqId, username);
        List<RequisitionTransaction> transactionList = transactionRepository.findAllByRequisitionIdAndIsActive(reqId, 1);
        List<EmployeeDTO> allActiveEmployees = masterClient.getEmployeeMasterList(xApiKey);
        List<Status> statusList = statusRepository.findAll();

        Map<String, Status> statusMap = statusList.stream()
                .collect(Collectors.toMap(Status::getStatusCode, Function.identity()));

        Map<Long, EmployeeDTO> employeeMap = allActiveEmployees.stream()
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

        return transactionList.stream().map(data -> {

            Status status = statusMap.get(data.getStatusCode());

            EmployeeDTO employeeBy = employeeMap.get(data.getActionBy());
            EmployeeDTO employeeTo = employeeMap.get(data.getActionTo());

            RequisitionTransactionDTO dto = new RequisitionTransactionDTO();

            dto.setTransactionId(data.getTransactionId());
            dto.setRequisitionId(data.getRequisitionId());
            dto.setActionDate(data.getActionDate());
            dto.setForwardBy(data.getActionBy());
            dto.setForwardByName(buildEmployeeName(employeeBy, true));
            dto.setForwardTo(data.getActionTo());
            dto.setForwardToName(buildEmployeeName(employeeTo, true));
            dto.setStatusCode(data.getStatusCode());
            dto.setStatusDetail(status != null ? status.getStatusName() : "");
            dto.setColorCode(status != null ? status.getColorCode() : "");
            dto.setRemarks(data.getRemarks());
            return dto;
        }).toList();
    }

    public static String getFinancialYear(LocalDate date) {

        int year = (date.getMonthValue() >= 4)
                ? date.getYear()
                : date.getYear() - 1;

        return year + "-" + (year + 1);
    }

    @Transactional
    public EvaluationRequestDTO addEvaluation(EvaluationRequestDTO dto, String username) {
        log.info("Request to add evaluation for empId {} by {} ", dto.getInitiator(), username);

        if (dto.getInitiator() == null) {
            throw new NotFoundException("Initiator can not be null");
        }
        Long initiatorId = dto.getInitiator();
        EvaluationDTO evaluationDTO = dto.getEvaluationData();
        Evaluation evaluation = new Evaluation();
        evaluation.setRequisitionId(evaluationDTO.getRequisitionId());
        evaluation.setTraineeId(initiatorId);
        evaluation.setImpact(evaluationDTO.getImpact());
        evaluation.setCreatedBy(username);
        evaluation.setCreatedDate(LocalDateTime.now());
        evaluation.setIsActive(1);

        evaluationRepository.save(evaluation);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<EvaluationRequestDTO> getEvaluationList(LocalDate fromDate, LocalDate toDate, String username) {
        log.info("Request to fetch evaluation list by {}", username);

        List<EvaluationDTO> list = evaluationRepository.findEvaluationData(fromDate, toDate);
        List<EmployeeDTO> allActiveEmployees = masterClient.getEmployeeMasterList(xApiKey);

        Map<Long, EmployeeDTO> employeeMap = allActiveEmployees.stream()
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

        return list.stream()
                .collect(Collectors.groupingBy(EvaluationDTO::getTraineeId))
                .entrySet()
                .stream()
                .map(entry -> {

                    Long traineeId = entry.getKey();
                    EmployeeDTO emp = employeeMap.get(traineeId);

                    if (emp == null) return null;

                    return new EvaluationRequestDTO(
                            traineeId,
                            emp.getEmpName(),
                            emp.getEmpDesigName(),
                            emp.getTitle() != null ? emp.getTitle() :
                                    (emp.getSalutation() != null ? emp.getSalutation() : ""),
                            entry.getValue(),
                            null
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(EvaluationRequestDTO::getEmpName))
                .toList();
    }

    public EvaluationRequestDTO getEvaluationPrint(Long id, String username) {
        log.info("Request to fetch Evaluation print data for id {} by {}", id, username);
        if (id == null) {
            throw new NotFoundException("Employee id cannot be null");
        }

        List<EvaluationDTO> evaluation = evaluationRepository.findByEmployee(id);

        List<EmployeeDTO> employeeDTOList = masterClient.getEmployee(xApiKey, id);
        EmployeeDTO employeeDTO = employeeDTOList.get(0);

        EvaluationRequestDTO requestDTO = new EvaluationRequestDTO();
        requestDTO.setInitiator(id);
        requestDTO.setTitle(employeeDTO.getTitle() != null ? employeeDTO.getTitle() :
                (employeeDTO.getSalutation() != null ? employeeDTO.getSalutation() : ""));
        requestDTO.setDesignation(employeeDTO.getEmpDesigName() != null ? employeeDTO.getEmpDesigName() : "");
        requestDTO.setEmpName(employeeDTO.getEmpName() != null ? employeeDTO.getEmpName() : "");
        requestDTO.setEvaluation(evaluation);

        return requestDTO;
    }

    public Optional<OrganizerDTO> getOrganizerById(@NotNull(message = "Organizer is required") Long organizerId) {
        log.info("Request to fetch organizer data for id {}", organizerId);
        return organizerRepository.findById(organizerId).map(organizerMapper::toDto);
    }

    public List<EligibilityDTO> getEligibilityList(String username) {
        log.info("Request to fetch eligibility list by {}", username);
        List<Eligibility> list = eligibilityRepository.findAllByIsActiveOrderByEligibilityIdDesc(1);
        return eligibilityMapper.toDto(list);
    }

    public EligibilityDTO addEligibleData(@Valid EligibilityDTO dto, String username) {
        log.info("Request to add eligibility name {} by {}", dto.getEligibilityName(), username);
        Eligibility eligibility = eligibilityMapper.toEntity(dto);
        eligibility.setCreatedBy(username);
        eligibility.setCreatedDate(LocalDateTime.now());
        eligibility.setIsActive(1);
        eligibility = eligibilityRepository.save(eligibility);

        return eligibilityMapper.toDto(eligibility);
    }

    @Transactional
    public RequisitionDTO revokeRequisition(@Valid RequisitionDTO dto, String username) {
        log.info("Request to revoke requisition for id {} ", dto.getRequisitionId());

        if (dto.getRequisitionId() == null) {
            throw new NotFoundException("Requisition Id can not be null");
        }

        Requisition requisition = requisitionRepository.findById(dto.getRequisitionId())
                .orElseThrow(() -> new NotFoundException("Requisition not found"));

        requisition.setStatus("REV");
        requisition.setModifiedBy(username);
        requisition.setModifiedDate(LocalDateTime.now());

        insertTransaction(dto.getRequisitionId(), dto.getActionBy(), dto.getActionBy(), username, "REV", null);
        return requisitionMapper.toDto(requisition);
    }

    @CacheEvict(value = "notificationList", allEntries = true)
    @Transactional
    public RequisitionDTO returnRequisition(@Valid RequisitionDTO dto, String username) {
        log.info("Request to return requisition for id {} ", dto.getRequisitionId());

        if (dto.getRequisitionId() == null) {
            throw new NotFoundException("Requisition Id can not be null");
        }

        Requisition requisition = requisitionRepository.findById(dto.getRequisitionId())
                .orElseThrow(() -> new NotFoundException("Requisition not found"));

        List<EmployeeDTO> allActiveEmployees = masterClient.getEmployeeMasterList(xApiKey);

        Map<Long, EmployeeDTO> employeeMap = allActiveEmployees.stream()
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

        EmployeeDTO employeeDTO = employeeMap.get(dto.getActionBy());

        if ("AR".equalsIgnoreCase(requisition.getStatus())) {
            requisition.setStatus("RR");
            insertTransaction(requisition.getRequisitionId(), dto.getActionBy(), requisition.getInitiatingOfficer(), username, "RR", dto.getRemarks());
        } else {
            requisition.setStatus("RV");
            insertTransaction(requisition.getRequisitionId(), dto.getActionBy(), requisition.getInitiatingOfficer(), username, "RV", dto.getRemarks());
        }
        requisition.setModifiedBy(username);
        requisition.setModifiedDate(LocalDateTime.now());

        String message = getNotificationMsg(requisition.getRequisitionNumber(), employeeDTO, "Returned by");
        insertNotification(dto.getActionBy(), requisition.getInitiatingOfficer(), "requisition", message, username);

        return requisitionMapper.toDto(requisition);
    }

    @Transactional(readOnly = true)
    public FeedbackDTO getFeedbackById(Long id, String username) {
        log.info("Request to fetch feedback data for id {} ", id);

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Feedback data not found"));

        FeedbackDTO dto = feedbackMapper.toDto(feedback);
        RequisitionDTO requisitionDTO = getRequisitionById(feedback.getRequisitionId(), username);

        List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);
        Map<Long, EmployeeDTO> employeeMap = employeeList != null
                ? employeeList.stream()
                .filter(e -> labCode != null && labCode.equalsIgnoreCase(e.getLabCode()))
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp))
                : Collections.emptyMap();

        EmployeeDTO employeeDTO = employeeMap.get(feedback.getParticipantId());

        if (employeeDTO != null) {
            dto.setParticipantName(buildEmployeeName(employeeDTO, true));
            dto.setDivisionName(employeeDTO.getEmpDivCode());
        }

        dto.setRequisitionId(requisitionDTO.getRequisitionId());
        dto.setRequisitionNumber(requisitionDTO.getRequisitionNumber());
        dto.setCourseName(requisitionDTO.getCourseName());
        dto.setOrganizer(requisitionDTO.getOrganizer());
        dto.setFromDate(requisitionDTO.getFromDate());
        dto.setToDate(requisitionDTO.getToDate());
        dto.setProgramDuration(requisitionDTO.getDuration());

        return dto;
    }

    @Transactional
    public Optional<FeedbackDTO> updateFeedback(@Valid FeedbackDTO dto, String username) {
        log.info("Request to update feedback for id {} by {}", dto.getFeedbackId(), username);

        if (dto.getFeedbackId() == null) {
            throw new NotFoundException("Feedback Id can not be null");
        }
        return feedbackRepository
                .findById(dto.getFeedbackId())
                .map(existingData -> {
                    existingData.setModifiedBy(username);
                    existingData.setModifiedDate(LocalDateTime.now());
                    feedbackMapper.partialUpdate(existingData, dto);
                    return existingData;
                })
                .map(feedbackRepository::save)
                .map(feedbackMapper::toDto);
    }

    @Transactional
    public FeedbackDTO acceptFeedback(FeedbackDTO dto, String username) {
        log.info("Request to accept feedback for id {} by {}", dto.getFeedbackId(), username);

        if (dto.getFeedbackId() == null) {
            throw new NotFoundException("Feedback Id can not be null");
        }

        Feedback feedback = feedbackRepository.findById(dto.getFeedbackId())
                .orElseThrow(() -> new NotFoundException("Feedback not found"));

        feedback.setIsAccepted("Y");
        feedback.setAcceptedBy(dto.getAcceptedBy());
        feedback.setAcceptedDate(LocalDateTime.now());
        feedback.setModifiedBy(username);
        feedback.setModifiedDate(LocalDateTime.now());

        return feedbackMapper.toDto(feedback);
    }

    public FeedbackDTO getFeedbackPrint(Long id, String username) {
        log.info("Request to print feedback for id {} by {}", id, username);

        if (id == null) {
            throw new NotFoundException("Feedback Id can not be null");
        }

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Feedback not found"));

        RequisitionDTO reqDTO = getRequisitionById(feedback.getRequisitionId(), username);

        EmployeeDTO employee = masterClient.getEmployee(xApiKey, feedback.getParticipantId()).get(0);

        FeedbackDTO dto = feedbackMapper.toDto(feedback);

        dto.setRequisitionNumber(reqDTO.getRequisitionNumber());
        dto.setCourseName(reqDTO.getCourseName());
        dto.setFromDate(reqDTO.getFromDate());
        dto.setToDate(reqDTO.getToDate());
        dto.setProgramDuration(reqDTO.getDuration());
        dto.setOrganizer(reqDTO.getOrganizer());
        dto.setParticipantName(buildEmployeeName(employee, true));
        dto.setDivisionName(employee.getEmpDivCode());

        if ("Y".equalsIgnoreCase(dto.getIsAccepted())) {
            EmployeeDTO acceptEmp = masterClient.getEmployee(xApiKey, feedback.getAcceptedBy()).get(0);
            dto.setAcceptedByName(buildEmployeeName(acceptEmp, true));
        }

        return dto;
    }

    private void insertTransaction(Long id, Long actionBy, Long actionTo, String username, String status, String remarks) {
        RequisitionTransaction transaction = new RequisitionTransaction();
        transaction.setRequisitionId(id);
        transaction.setStatusCode(status);
        transaction.setActionBy(actionBy);
        transaction.setActionTo(actionTo);
        transaction.setActionDate(LocalDateTime.now());
        transaction.setRemarks(remarks);
        transaction.setCreatedBy(username);
        transaction.setCreatedDate(LocalDateTime.now());
        transaction.setIsActive(1);
        transactionRepository.save(transaction);
    }


    private void insertNotification(Long actionBy, Long actionTo, String url, String message, String username) {
        Notification notification = new Notification();
        notification.setNotificationBy(actionBy);
        notification.setEmpId(actionTo);
        notification.setNotificationDate(LocalDate.now());
        notification.setNotificationUrl(url);
        notification.setNotificationMessage(message);
        notification.setCreatedBy(username);
        notification.setCreatedDate(LocalDateTime.now());
        notification.setIsActive(1);
        notificationRepository.save(notification);
    }

}
