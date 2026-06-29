package com.vts.hrms.mapper;

import com.vts.hrms.dto.HandingOverDTO;
import com.vts.hrms.entity.HandingOver;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HandingOverMapper extends EntityMapper<HandingOverDTO, HandingOver>{
}
