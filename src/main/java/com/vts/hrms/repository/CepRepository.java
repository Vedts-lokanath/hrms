package com.vts.hrms.repository;

import com.vts.hrms.entity.Cep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CepRepository extends JpaRepository<Cep, Long> {

    List<Cep> findAllByIsActive(Integer isActive);
}
