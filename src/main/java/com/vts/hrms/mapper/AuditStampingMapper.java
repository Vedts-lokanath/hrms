package com.vts.hrms.mapper;

import com.vts.hrms.dto.AuditStampingDTO;
import org.mapstruct.Mapper;
import com.vts.hrms.entity.AuditStamping;

@Mapper(componentModel = "spring")
public interface AuditStampingMapper extends  EntityMapper <AuditStampingDTO,AuditStamping>{
}
