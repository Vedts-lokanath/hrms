package com.vts.hrms.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
public class NotificationDTO implements Serializable {

    private Long notificationId;
    private Long empId;
    private String empName;
    private String empDesig;
    private Long notificationBy;
    private String notificationUrl;
    private LocalDate notificationDate;
    private String notificationMessage;

}
