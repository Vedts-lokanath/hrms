package com.vts.hrms.mapper;

import com.vts.hrms.dto.SponsorshipDTO;
import com.vts.hrms.entity.Sponsorship;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SponsorshipMapper extends EntityMapper<SponsorshipDTO, Sponsorship> {
}


