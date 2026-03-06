package com.vts.hrms.service;

import com.vts.hrms.dto.*;
import com.vts.hrms.entity.*;
import com.vts.hrms.entity.Calendar;
import com.vts.hrms.entity.Program;
import com.vts.hrms.entity.Requisition;
import com.vts.hrms.exception.NotFoundException;
import com.vts.hrms.mapper.*;
import com.vts.hrms.repository.*;
import com.vts.hrms.util.FileStorageUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private final MasterClientService masterClient;
    private final OrganizerRepository organizerRepository;
    private final AgencyMapper agencyMapper;
    private final CalendarMapper calenderMapper;
    private final CalenderRepository calenderRepository;
    private final ProgramMapper programMapper;
    private final ProgramRepository programRepository;
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

    public TrainingService(MasterClientService masterClient, OrganizerRepository organizerRepository, AgencyMapper agencyMapper, CalendarMapper calenderMapper, CalenderRepository calenderRepository, ProgramMapper programMapper, ProgramRepository programRepository, RequisitionMapper requisitionMapper, RequisitionRepository requisitionRepository, FeedbackMapper feedbackMapper, FeedbackRepository feedbackRepository, RequisitionTransactionRepository transactionRepository, StatusRepository statusRepository, NotificationRepository notificationRepository, SignRoleAuthorityRepository signRoleAuthorityRepository, RequisitionSequenceRepository sequenceRepository, EvaluationRepository evaluationRepository) {
        this.masterClient = masterClient;
        this.organizerRepository = organizerRepository;
        this.agencyMapper = agencyMapper;
        this.calenderMapper = calenderMapper;
        this.calenderRepository = calenderRepository;
        this.programMapper = programMapper;
        this.programRepository = programRepository;
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
    }

    @Transactional(readOnly = true)
    public List<OrganizerIdDTO> getAllAgencies() {
        List<Organizer> list = organizerRepository.findAllByIsActive(1);
        list = list.stream().sorted(Comparator.comparing(Organizer::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()))).toList();
        return list.stream().map(agencyMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Transactional
    public CalendarDTO addCalenderData(CalendarDTO dto, String username) throws IOException {
        log.info("Request to save calender for year {} by {}", dto.getYear(), username);
        Calendar calender = calenderMapper.toEntity(dto);
        calender.setCreatedBy(username);
        calender.setCreatedDate(LocalDateTime.now());
        calender.setIsActive(1);

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            Path fullpath = Paths.get(appStorage, "Calendar", dto.getYear());
            calender.setFileName(dto.getFile().getOriginalFilename());
            FileStorageUtil.saveFile(fullpath, dto.getFile().getOriginalFilename(), dto.getFile());
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
                dto.setAgency(organizer.getOrganizer());
            }
        });
        return dtoList;
    }

    public Optional<CalendarDTO> getCalendarById(Long id, String username) {
        return calenderRepository.findById(id).map(calenderMapper::toDto);
    }

    @Transactional
    public ProgramDTO addProgramData(@Valid ProgramDTO dto, String username) {
        log.info("Request to add program {} by {}", dto.getProgramName(), username);
        Program program = programMapper.toEntity(dto);
        program.setCreatedBy(username);
        program.setCreatedDate(LocalDateTime.now());
        program.setIsActive(1);
        program = programRepository.save(program);

        Organizer organizer = organizerRepository.findById(program.getOrganizerId())
                .orElseThrow(() -> new NotFoundException("Organizer data not found"));

        ProgramDTO programDTO = programMapper.toDto(program);
        programDTO.setOrganizer(organizer.getOrganizer());

        return programDTO;
    }

    @Transactional(readOnly = true)
    public List<ProgramDTO> getProgramList(String username) {
        log.info("Program list fetched by {}", username);
        List<Organizer> organizerList = organizerRepository.findAllByIsActive(1);
        List<Program> programList = programRepository.findAllByIsActive(1);
        programList = programList.stream()
                .sorted(Comparator.comparing(Program::getCreatedDate).reversed())
                .toList();

        Map<Long, Organizer> organizerMap = organizerList.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        List<ProgramDTO> dtoList = programMapper.toDto(programList);

        dtoList.forEach(dto -> {
            Organizer organizer = organizerMap.get(dto.getOrganizerId());
            if (organizer != null) {
                dto.setOrganizer(organizer.getOrganizer());
            }
        });
        return dtoList;
    }

    @Transactional
    public RequisitionDTO addRequisitionData(@Valid RequisitionDTO dto, String username) throws IOException {
        log.info("Request to add requisition for program {} by {}", dto.getProgramName(), username);

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
        insertTransaction(requisition.getRequisitionId(), requisition.getInitiatingOfficer(), requisition.getInitiatingOfficer(), username, "AA");
        return requisitionMapper.toDto(requisition);
    }

    @Transactional(readOnly = true)
    public List<RequisitionDTO> getRequisitionList(String username) {
        log.info("Requisition list fetched by {}", username);
        List<Organizer> organizerList = organizerRepository.findAllByIsActive(1);
        List<Program> programList = programRepository.findAllByIsActive(1);
        List<Requisition> list = requisitionRepository.findAllByIsActiveOrderByRequisitionIdDesc(1);
        List<Status> statusList = statusRepository.findAll();

        List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);

        Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

        Map<Long, Organizer> organizerMap = organizerList.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        Map<Long, Program> programMap = programList.stream()
                .collect(Collectors.toMap(Program::getProgramId, Function.identity()));

        Map<String, Status> statusMap = statusList.stream()
                .collect(Collectors.toMap(Status::getStatusCode, Function.identity()));

        List<RequisitionDTO> dtoList = requisitionMapper.toDto(list);

        dtoList.forEach(dto -> {
            EmployeeDTO employeeDTO = employeeMap.get(dto.getInitiatingOfficer());
            Program program = programMap.get(dto.getProgramId());
            Organizer organizer = organizerMap.get(program.getOrganizerId());
            dto.setProgramName(program.getProgramName());
            dto.setVenue(program.getVenue());

            Status status = statusMap.get(dto.getStatus());
            dto.setStatusName(status.getStatusName());
            dto.setRegistrationFee(program.getRegistrationFee());
            if (organizer != null) {
                dto.setOrganizer(organizer.getOrganizer());
                dto.setOrganizerContactName(organizer.getContactName());
                dto.setOrganizerPhoneNo(organizer.getPhoneNo());
                dto.setOrganizerFaxNo(organizer.getFaxNo());
                dto.setOrganizerEmail(organizer.getEmail());
            }
            if (employeeDTO != null) {
                dto.setEmpNo(employeeDTO.getEmpNo());
                dto.setInitiatingOfficerName((employeeDTO.getTitle() != null ? employeeDTO.getTitle() : "") + " " + employeeDTO.getEmpName());
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
        List<Program> programList = programRepository.findAllByIsActive(1);

        Map<Long, Organizer> organizerMap = organizerList.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        Map<Long, Program> programMap = programList.stream()
                .collect(Collectors.toMap(Program::getProgramId, Function.identity()));

        Requisition requisition = requisitionRepository.findById(id).orElseThrow(() -> new NotFoundException("Requisition not found"));

        Program program = programMap.get(requisition.getProgramId());
        Organizer org = organizerMap.get(program.getOrganizerId());

        RequisitionDTO requisitionDTO = requisitionMapper.toDto(requisition);
        requisitionDTO.setOrganizer(org.getOrganizer());
        requisitionDTO.setOrganizerId(org.getOrganizerId());
        requisitionDTO.setProgramName(program.getProgramName());
        requisitionDTO.setVenue(program.getVenue());
        requisitionDTO.setRegistrationFee(program.getRegistrationFee());
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
        feedback.setCreatedBy(username);
        feedback.setCreatedDate(LocalDateTime.now());
        feedback.setIsActive(1);
        feedback = feedbackRepository.save(feedback);
        return feedbackMapper.toDto(feedback);
    }

    public List<FeedbackDTO> getFeedbackList(String username) {
        log.info("Feedback list fetched by {}", username);
        List<Feedback> feedbackList = feedbackRepository.findByIsActive(1);

        if (feedbackList == null || feedbackList.isEmpty()) {
            return Collections.emptyList();
        }

        List<FeedbackDTO> feedbbackdto = feedbackMapper.toDto(feedbackList);

        if (feedbbackdto == null || feedbbackdto.isEmpty()) {
            return Collections.emptyList();
        }

        List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);

        Map<Long, EmployeeDTO> employeeMap = employeeList != null
                ? employeeList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp))
                : Collections.emptyMap();

        feedbbackdto.forEach(d -> {

            if (d == null) return;

            EmployeeDTO employeeDTO = employeeMap.get(d.getParticipantId());

            if (employeeDTO != null) {

                String title = employeeDTO.getTitle() != null ? employeeDTO.getTitle() : "";
                String empName = employeeDTO.getEmpName() != null ? employeeDTO.getEmpName() : "";
                String designation = employeeDTO.getEmpDesigName() != null ? employeeDTO.getEmpDesigName() : "";

                String participantName =
                        (title.isEmpty() ? "" : title + " ")
                                + empName
                                + (designation.isEmpty() ? "" : ", " + designation);

                d.setParticipantName(participantName.trim());
                d.setDivisionName(employeeDTO.getEmpDivCode());
            }

            RequisitionDTO requisitionDto = null;

            if (d.getRequisitionId() != null) {
                requisitionDto = getRequisitionById(d.getRequisitionId(), username);
            }

            if (requisitionDto != null) {
                d.setProgramName(requisitionDto.getProgramName());
                d.setOrganizer(requisitionDto.getOrganizer());
                d.setFromDate(requisitionDto.getFromDate());
                d.setToDate(requisitionDto.getToDate());
                d.setProgramDuration(requisitionDto.getDuration());
            }

        });

        return feedbbackdto;
    }

    public Optional<ProgramDTO> editProgramData(@Valid ProgramDTO dto, String username) {
        log.info("Request to edit program {} by {}", dto.getProgramName(), username);

        return programRepository
                .findById(dto.getProgramId())
                .map(existingProgram -> {
                    existingProgram.setModifiedBy(username);
                    existingProgram.setModifiedDate(LocalDateTime.now());
                    programMapper.partialUpdate(existingProgram, dto);
                    return existingProgram;
                })
                .map(programRepository::save)
                .map(programMapper::toDto);
    }

    public OrganizerIdDTO addOrganizer(@Valid OrganizerIdDTO dto, String username) {
        log.info("Request to add organizer {} by {}", dto.getOrganizer(), username);
        Organizer organizer = agencyMapper.toEntity(dto);
        organizer.setCreatedBy(username);
        organizer.setCreatedDate(LocalDateTime.now());
        organizer.setIsActive(1);
        organizer = organizerRepository.save(organizer);

        return agencyMapper.toDto(organizer);
    }

    public Optional<OrganizerIdDTO> editOrganizer(@Valid OrganizerIdDTO dto, String username) {
        log.info("Request to edit organizer {} by {}", dto.getOrganizer(), username);

        return organizerRepository
                .findById(dto.getOrganizerId())
                .map(organizer -> {
                    organizer.setModifiedBy(username);
                    organizer.setModifiedDate(LocalDateTime.now());
                    agencyMapper.partialUpdate(organizer, dto);
                    return organizer;
                })
                .map(organizerRepository::save)
                .map(agencyMapper::toDto);
    }

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
                .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

        EmployeeDTO employeeDTO = employeeMap.get(requisition.getInitiatingOfficer());

        String message = getNotificationMsg(requisition.getRequisitionNumber(), employeeDTO);

        DivisionDTO divisionDTO = Optional.of(employeeDTO)
                .map(EmployeeDTO::getDivisionId)
                .flatMap(id -> Optional.ofNullable(masterClient.getDivisionMaster(xApiKey))
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(d -> id.equals(d.getDivisionId()))
                        .findFirst())
                .orElseThrow(() -> new NotFoundException("Division data not found"));

        insertTransaction(dto.getRequisitionId(), dto.getActionBy(), divisionDTO.getDivisionHeadId(), username, "AF");
        insertNotification(dto.getActionBy(), divisionDTO.getDivisionHeadId(), "req-approval", message, username);
        return requisitionMapper.toDto(requisition);
    }

    @Transactional
    public RequisitionDTO recommendRequisition(@Valid RequisitionDTO dto, String username) {
        log.info("Request to recommend requisition for id {} ", dto.getRequisitionId());

        if (dto.getRequisitionId() == null) {
            throw new NotFoundException("Requisition Id can not be null");
        }

        Requisition requisition = requisitionRepository.findById(dto.getRequisitionId())
                .orElseThrow(() -> new NotFoundException("Requisition not found"));

        if(requisition.getStatus().equalsIgnoreCase("AR")){
            requisition.setStatus("AV");
            insertTransaction(dto.getRequisitionId(), dto.getActionBy(), dto.getActionBy(), username, "AV");
        }else {
            requisition.setStatus("AR");

            SignRoleAuthorityDTO authorityDTO = signRoleAuthorityRepository.findBySignAuthRole("AD-HRT");
            if (authorityDTO.getSignRoleAuthorityId() == null) {
                throw new NotFoundException("In SignRoleAuthority AD-HRT role not found");
            }

            List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);

            Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
                    .collect(Collectors.toMap(EmployeeDTO::getEmpId, emp -> emp));

            EmployeeDTO employeeDTO = employeeMap.get(dto.getActionBy());

            String message = getNotificationMsg(requisition.getRequisitionNumber(), employeeDTO);
            insertTransaction(dto.getRequisitionId(), dto.getActionBy(), authorityDTO.getEmpId(), username, "AR");
            insertNotification(dto.getActionBy(), authorityDTO.getEmpId(), "req-approval", message, username);
        }
        requisition.setModifiedBy(username);
        requisition.setModifiedDate(LocalDateTime.now());
        return requisitionMapper.toDto(requisition);
    }

    private static String getNotificationMsg(String requisitionNumber, EmployeeDTO employeeDTO) {
        String prefix = employeeDTO.getTitle() != null && !employeeDTO.getTitle().trim().isEmpty()
                ? employeeDTO.getTitle()
                : (employeeDTO.getSalutation() != null ? employeeDTO.getSalutation() : "");

        return String.format(
                "Requisition no " + requisitionNumber + " forwarded by %s%s%s",
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
        List<Program> programList = programRepository.findAllByIsActive(1);
        List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);

        // Convert to maps safely
        Map<Long, Organizer> organizerMap = organizerList.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        Map<Long, Program> programMap = programList.stream()
                .collect(Collectors.toMap(Program::getProgramId, Function.identity()));

        Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
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
        // Program & Organizer
        // =========================
        Program program = programMap.get(requisition.getProgramId());

        if (program != null) {
            dto.setProgramName(program.getProgramName());
            dto.setVenue(program.getVenue());
            dto.setRegistrationFee(program.getRegistrationFee());

            Organizer organizer = organizerMap.get(program.getOrganizerId());
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

        if(forwardTxn !=null) dto.setForwardDate(forwardTxn.getActionDate());

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
        List<Program> programList = programRepository.findAllByIsActive(1);
        List<EmployeeDTO> employeeList = masterClient.getEmployeeMasterList(xApiKey);
        List<Status> statusList = statusRepository.findAll();

        Map<String, Status> statusMap = statusList.stream()
                .collect(Collectors.toMap(Status::getStatusCode, Function.identity()));

        Map<Long, Organizer> organizerMap = organizerList.stream()
                .collect(Collectors.toMap(Organizer::getOrganizerId, Function.identity()));

        Map<Long, Program> programMap = programList.stream()
                .collect(Collectors.toMap(Program::getProgramId, Function.identity()));

        Map<Long, EmployeeDTO> employeeMap = employeeList.stream()
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

            // Program + Organizer
            Program program = programMap.get(dto.getProgramId());
            if (program != null) {

                dto.setProgramName(program.getProgramName());
                dto.setVenue(program.getVenue());
                dto.setRegistrationFee(program.getRegistrationFee());

                Organizer organizer = organizerMap.get(program.getOrganizerId());
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
            if (status!=null){
                dto.setStatusName(status.getStatusName());
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
        List<RequisitionTransaction> transactionList = transactionRepository.findAllByRequisitionIdAndIsActive(reqId,1);
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
            dto.setForwardByName(employeeBy != null ? employeeBy.getEmpName() + ", " + employeeBy.getEmpDesigName() : "");
            dto.setForwardTo(data.getActionTo());
            dto.setForwardToName(employeeTo != null ? employeeTo.getEmpName() + ", " + employeeTo.getEmpDesigName() : "");
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
                            emp.getTitle()!=null ? emp.getTitle() :
                                    (emp.getSalutation()!=null ? emp.getSalutation() : ""),
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

        List<EmployeeDTO> employeeDTOList = masterClient.getEmployee(xApiKey,id);
        EmployeeDTO employeeDTO = employeeDTOList.get(0);

        EvaluationRequestDTO requestDTO = new EvaluationRequestDTO();
        requestDTO.setInitiator(id);
        requestDTO.setTitle(employeeDTO.getTitle()!=null ? employeeDTO.getTitle() :
                (employeeDTO.getSalutation()!=null ? employeeDTO.getSalutation() : ""));
        requestDTO.setDesignation(employeeDTO.getEmpDesigName()!=null ? employeeDTO.getEmpDesigName() : "");
        requestDTO.setEmpName(employeeDTO.getEmpName()!=null ? employeeDTO.getEmpName() : "");
        requestDTO.setEvaluation(evaluation);

        return requestDTO;
    }

    private void insertTransaction(Long id, Long actionBy, Long actionTo, String username, String status) {
        RequisitionTransaction transaction = new RequisitionTransaction();
        transaction.setRequisitionId(id);
        transaction.setStatusCode(status);
        transaction.setActionBy(actionBy);
        transaction.setActionTo(actionTo);
        transaction.setActionDate(LocalDateTime.now());
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
