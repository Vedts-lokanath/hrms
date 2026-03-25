package com.vts.hrms.mapper;

import com.vts.hrms.dto.CourseDTO;
import com.vts.hrms.entity.Course;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper extends EntityMapper<CourseDTO, Course> {
}
