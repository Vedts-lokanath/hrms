package com.vts.hrms.mapper;

import com.vts.hrms.dto.EligibilityDTO;
import com.vts.hrms.entity.Eligibility;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EligibilityMapper extends EntityMapper<EligibilityDTO, Eligibility>{
}
