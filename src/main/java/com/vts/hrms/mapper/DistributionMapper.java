package com.vts.hrms.mapper;
import com.vts.hrms.dto.DistributionDTO;
import com.vts.hrms.entity.Distribution;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DistributionMapper extends EntityMapper<DistributionDTO, Distribution> {
}
