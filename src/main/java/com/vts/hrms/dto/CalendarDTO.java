package com.vts.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CalendarDTO implements Serializable {

    private Long calendarId;

    @NotNull(message = "Organizer is required")
    private Long organizerId;
    private String organizer;

    @NotBlank(message = "Year is required")
    private String year;

    private String calendarFileName;
    private String coveringLetter;
    private MultipartFile file;
    private MultipartFile coverFile;
    private String createdBy;
    private LocalDateTime createdDate;

}
