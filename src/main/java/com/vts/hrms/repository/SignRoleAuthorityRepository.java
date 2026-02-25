package com.vts.hrms.repository;

import com.vts.hrms.entity.SignRoleAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignRoleAuthorityRepository extends JpaRepository<SignRoleAuthority, Long> {

    List<SignRoleAuthority> findAllByIsActive(int isActive);
}
