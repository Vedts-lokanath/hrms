package com.vts.hrms.mapper;

import com.vts.hrms.dto.CepDTO;
import com.vts.hrms.entity.Cep;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface  CepMapper extends EntityMapper<CepDTO, Cep> {
}
