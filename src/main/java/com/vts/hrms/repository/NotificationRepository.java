package com.vts.hrms.repository;

import com.vts.hrms.entity.Notification;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = "SELECT COUNT(r) FROM Notification r WHERE r.isActive=1 AND r.empId=:empId")
    int getNotificationCount(@Param("empId") Long empId);

    @Query("SELECT r FROM Notification r WHERE r.isActive=1 AND r.empId=:empId ORDER BY r.notificationId DESC")
    List<Notification> getNotificationList(@Param("empId") Long empId);
}
