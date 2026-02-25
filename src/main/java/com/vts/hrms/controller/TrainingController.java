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

            Path filePath = Paths.get(appStorage, "Requisition", file);

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

}
