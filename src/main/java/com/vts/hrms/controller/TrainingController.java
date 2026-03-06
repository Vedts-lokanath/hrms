package com.vts.hrms.controller;

import com.vts.hrms.dto.*;
import com.vts.hrms.exception.NotFoundException;
import com.vts.hrms.service.TrainingService;
import com.vts.hrms.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/training")
public class TrainingController {

    @Value("${appStorage}")
    private String appStorage;

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping(value = "/agency")
    public ResponseEntity<ApiResponse> getAllAgencies() {
        List<OrganizerIdDTO> list = trainingService.getAllAgencies();

        return ResponseEntity.ok(
                new ApiResponse(true, "Organizer list fetched", list)
        );
    }

    @GetMapping(value = "/calender")
    public ResponseEntity<ApiResponse> getCalenderList(@RequestParam String year, @RequestHeader String username) {
        List<CalendarDTO> list = trainingService.getCalenderList(year,username);

        return ResponseEntity.ok(
                new ApiResponse(true, "Calender list fetched", list)
        );
    }

    @PostMapping(value = "/add-calendar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> addCalenderData(@Valid @ModelAttribute CalendarDTO dto, @RequestHeader String username) throws IOException {
        CalendarDTO data = trainingService.addCalenderData(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Calender data added successfully", data)
        );
    }

    @GetMapping(value = "/calendar-file/{id}")
    public ResponseEntity<Resource> downloadCalendarFile(@PathVariable Long id, @RequestHeader String username) {
        try {
            CalendarDTO dto = trainingService.getCalendarById(id, username)
                    .orElseThrow(()-> new NotFoundException("Calendar file not found"));

            Path filePath = Paths.get(appStorage, "Calendar", dto.getYear(), dto.getFileName());

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            String fileName = dto.getFileName();
            String contentType = Files.probeContentType(filePath);

            // Fallback content type
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/add-program")
    public ResponseEntity<ApiResponse> addProgramData(@Valid @RequestBody ProgramDTO dto, @RequestHeader String username) throws IOException {
        ProgramDTO data = trainingService.addProgramData(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Program data added successfully", data)
        );
    }

    @PutMapping(value = "/edit-program")
    public ResponseEntity<ApiResponse> editProgramData(@Valid @RequestBody ProgramDTO dto, @RequestHeader String username) throws IOException {
        Optional<ProgramDTO> data = trainingService.editProgramData(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Program data edited successfully", data)
        );
    }

    @PostMapping(value = "/add-organizer")
    public ResponseEntity<ApiResponse> addOrganizer(@Valid @RequestBody OrganizerIdDTO dto, @RequestHeader String username) throws IOException {
        OrganizerIdDTO data = trainingService.addOrganizer(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Organizer data added successfully", data)
        );
    }

    @PutMapping(value = "/edit-organizer")
    public ResponseEntity<ApiResponse> editOrganizer(@Valid @RequestBody OrganizerIdDTO dto, @RequestHeader String username) throws IOException {
        Optional<OrganizerIdDTO> data = trainingService.editOrganizer(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Organizer data edited successfully", data)
        );
    }

    @GetMapping(value = "/program")
    public ResponseEntity<ApiResponse> getProgramList(@RequestHeader String username) {
        List<ProgramDTO> list = trainingService.getProgramList(username);

        return ResponseEntity.ok(
                new ApiResponse(true, "Program list fetched", list)
        );
    }

    @PostMapping(value = "/add-requisition", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> addRequisitionData(@Valid @ModelAttribute RequisitionDTO dto, @RequestHeader String username) throws IOException {
        RequisitionDTO data = trainingService.addRequisitionData(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Requisition data added successfully", data)
        );
    }

    @GetMapping(value = "/requisition")
    public ResponseEntity<ApiResponse> getRequisitionList(@RequestHeader String username) {
        List<RequisitionDTO> list = trainingService.getRequisitionList(username);

        return ResponseEntity.ok(
                new ApiResponse(true, "Requisition list fetched", list)
        );
    }

    @GetMapping(value = "/requisition/{id}")
    public ResponseEntity<ApiResponse> getRequisitionById(@PathVariable Long id, @RequestHeader String username) {
        RequisitionDTO data = trainingService.getRequisitionById(id,username);

        return ResponseEntity.ok(
                new ApiResponse(true, "Requisition data fetched", data)
        );
    }

    @GetMapping(value = "/req-file/{id}/{file}")
    public ResponseEntity<Resource> downloadRequisitionFile(@PathVariable Long id, @PathVariable String file, @RequestHeader String username) {
        try {

            RequisitionDTO requisitionDTO = trainingService.getRequisitionById(id, username);
            Path filePath = Paths.get(appStorage, "Requisition", requisitionDTO.getRequisitionNumber().replace("/","_"), file);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);

            // Fallback content type
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping(value = "/update-requisition", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> updateRequisitionData(@Valid @ModelAttribute RequisitionDTO dto, @RequestHeader String username) throws IOException {
        Optional<RequisitionDTO> data = trainingService.updateRequisitionData(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Requisition data updated successfully", data)
        );
    }

    @PostMapping(value = "/requisition-feedback")
    public ResponseEntity<ApiResponse> requisitionFeedback(@Valid @RequestBody FeedbackDTO dto, @RequestHeader String username) throws IOException {
        FeedbackDTO data = trainingService.requisitionFeedback(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Feedback submitted successfully", data)
        );
    }

    @GetMapping(value = "/feedback-list")
    public ResponseEntity<ApiResponse> getFeedbackList(@RequestHeader String username) {
        List<FeedbackDTO> list = trainingService.getFeedbackList(username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Feedback list fetched successfully", list)
        );
    }

    @PostMapping(value = "/forward-req")
    public ResponseEntity<ApiResponse> forwardRequisition(@Valid @RequestBody RequisitionDTO dto, @RequestHeader String username) {
        RequisitionDTO data = trainingService.forwardRequisition(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Requisition forwarded successfully", data)
        );
    }

    @PostMapping(value = "/recommend-req")
    public ResponseEntity<ApiResponse> recommendRequisition(@Valid @RequestBody RequisitionDTO dto, @RequestHeader String username) {
        RequisitionDTO data = trainingService.recommendRequisition(dto,username);
        String message = data.getStatus().equalsIgnoreCase("AR") ? "Requisition recommended successfully" : "Requisition approved successfully";
        return ResponseEntity.ok(
                new ApiResponse(true, message, data)
        );
    }

    @GetMapping(value = "/requisition-print/{id}")
    public ResponseEntity<ApiResponse> getRequisitionPrint(@PathVariable Long id, @RequestHeader String username) {
        RequisitionDTO data = trainingService.getRequisitionPrint(id,username);

        return ResponseEntity.ok(
                new ApiResponse(true, "Requisition print data fetched", data)
        );
    }

    @GetMapping(value = "/req-approval-list")
    public ResponseEntity<ApiResponse> getRequisitionApprovalList(@RequestParam Long empId, @RequestHeader String username) {
        List<RequisitionDTO> list = trainingService.getRequisitionApprovalList(empId,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Requisition Approval list fetched successfully", list)
        );
    }

    @GetMapping(value = "/req-transaction/{reqId}")
    public ResponseEntity<ApiResponse> getRequisitionTransaction(@PathVariable Long reqId, @RequestHeader String username) {
        List<RequisitionTransactionDTO> list = trainingService.getRequisitionTransaction(reqId,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Requisition transaction fetched successfully", list)
        );
    }

    @PostMapping(value = "/add-evaluation")
    public ResponseEntity<ApiResponse> addEvaluation(@RequestBody EvaluationRequestDTO dto, @RequestHeader String username) {
        EvaluationRequestDTO data = trainingService.addEvaluation(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Evaluation added successfully", data)
        );
    }

    @GetMapping(value = "/evaluation")
    public ResponseEntity<ApiResponse> getEvaluationList(@RequestParam LocalDate fromDate, @RequestParam LocalDate toDate, @RequestHeader String username) {
        List<EvaluationRequestDTO> list = trainingService.getEvaluationList(fromDate, toDate, username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Evaluation list fetched", list)
        );
    }

    @GetMapping(value = "/evaluation-print/{id}")
    public ResponseEntity<ApiResponse> getEvaluationPrint(@PathVariable Long id, @RequestHeader String username) {
        EvaluationRequestDTO data = trainingService.getEvaluationPrint(id,username);

        return ResponseEntity.ok(
                new ApiResponse(true, "Evaluation print data fetched", data)
        );
    }

}
