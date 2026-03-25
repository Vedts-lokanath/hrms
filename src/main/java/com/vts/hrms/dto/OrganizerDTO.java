package com.vts.hrms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrganizerDTO {

    private Long organizerId;

    @NotBlank(message = "Organizer is required")
    private String organizer;

    private String contactName;

    @Size(min = 10, max = 10, message = "Phone number must be 10 digits")
    private String phoneNo;

    private String faxNo;

    @Email(message = "Invalid email address")
    private String email;

}
