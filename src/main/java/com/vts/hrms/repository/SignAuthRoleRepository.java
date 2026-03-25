package com.vts.hrms.repository;

import com.vts.hrms.entity.SignAuthRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignAuthRoleRepository extends JpaRepository<SignAuthRole, Long> {

    List<SignAuthRole> findAllByIsActive(int isActive);
}
