package com.vts.hrms.repository;

import com.vts.hrms.entity.MandatoryTraining;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MandatoryTrainingRepository extends JpaRepository<MandatoryTraining, Long> {

    List<MandatoryTraining> findAllByIsActiveOrderByMandatoryTrainingIdDesc(int isActive);

    List<MandatoryTraining> findAllByParticipantIdAndIsActiveOrderByMandatoryTrainingIdDesc(Long empId, int isActive);
}
