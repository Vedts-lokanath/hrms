package com.vts.hrms.repository;

import com.vts.hrms.entity.FormDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FormDetailRepository extends JpaRepository<FormDetail, Long> {

    @Query(value = """
	            SELECT DISTINCT b.*
	            FROM hrms_form_detail b JOIN hrms_form_role_access c ON b.form_detail_id = c.form_detail_id
	            WHERE c.is_active = 1 AND c.for_view="Y" AND c.role_id = :roleId AND b.is_active = 1
	            ORDER BY b.form_module_id, b.form_serial_no
	            """, nativeQuery = true)
    List<FormDetail> findDistinctFormModulesDetailsByRoleId(@Param("roleId") Long roleId);

}
