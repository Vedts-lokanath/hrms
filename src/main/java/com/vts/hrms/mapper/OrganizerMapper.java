package com.vts.hrms.mapper;

import com.vts.hrms.dto.OrganizerDTO;
import com.vts.hrms.entity.Organizer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizerMapper extends EntityMapper<OrganizerDTO, Organizer> {

}