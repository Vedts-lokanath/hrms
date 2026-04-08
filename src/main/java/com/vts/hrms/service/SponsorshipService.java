package com.vts.hrms.service;

import com.vts.hrms.dto.*;
import com.vts.hrms.entity.*;
import com.vts.hrms.exception.NotFoundException;
import com.vts.hrms.mapper.SponsorshipMapper;
import com.vts.hrms.repository.SponsorshipRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SponsorshipService {

    private static final Logger log = LoggerFactory.getLogger(SponsorshipService.class);

    @Value("${x_api_key}")
    private String xApiKey;

    private final SponsorshipRepository sponsorshipRepository;
    private final SponsorshipMapper sponsorshipMapper;
    private final MasterClientService masterClientService;

    public SponsorshipService(SponsorshipRepository sponsorshipRepository, SponsorshipMapper sponsorshipMapper, MasterClientService masterClientService) {
        this.sponsorshipRepository = sponsorshipRepository;
        this.sponsorshipMapper = sponsorshipMapper;
        this.masterClientService = masterClientService;
    }


    public List<SponsorshipDTO> getAllSponsorshipList(String username) {

        log.info("Request to fetch sponsorship list by {}", username);

        List<Sponsorship> list = sponsorshipRepository.findAllByIsActive(1);
        List<SponsorshipDTO> dtoList = sponsorshipMapper.toDto(list);
        List<EmployeeDTO> employeeDTOList = masterClientService.getEmployeeMasterList(xApiKey);

        Map<Long, EmployeeDTO> employeeDTOMap = employeeDTOList.stream()
                        .collect(Collectors.toMap
                                (EmployeeDTO::getEmpId, Function.identity()
                                ));

        dtoList.forEach(data->{
            EmployeeDTO employeeDTO = employeeDTOMap.get(data.getEmpId());
            data.setEmployeeName(employeeDTO.getEmpName() + ", " + employeeDTO.getEmpDesigName());
        });

        return dtoList;
    }


    @CacheEvict(value = "SponsorshipCache", allEntries = true)
    @Transactional
    public SponsorshipDTO addSponsorshipData(@Valid SponsorshipDTO dto, String username) {
        log.info("Request to add sponsorship by {}", username);

        Sponsorship sponsorship = sponsorshipMapper.toEntity(dto);

        sponsorship.setCreatedBy(username);
        sponsorship.setCreatedDate(LocalDateTime.now());
        sponsorship.setIsActive(1);

        sponsorship = sponsorshipRepository.save(sponsorship);

        return sponsorshipMapper.toDto(sponsorship);
    }


    @CacheEvict(value = "SponsorshipCache", allEntries = true)
    @Transactional
    public Optional<SponsorshipDTO> EditSponsorshipData(SponsorshipDTO dto, String username) {
        log.info("Request to edit program id {} by {}", dto.getSponsorshipId(), username);

        return sponsorshipRepository.findById(dto.getSponsorshipId())
                .map(existing -> {
                    existing.setModifiedBy(username);
                    existing.setModifiedDate(LocalDateTime.now());
                        sponsorshipMapper.partialUpdate(existing, dto);
                         return existing;
                })
                .map(sponsorshipRepository::save)
                .map(sponsorshipMapper::toDto);
    }


    @Transactional
    public SponsorshipDTO getSponsorshipById(Long sponsorshipId, String username) {

        log.info("Request to fetch Sponsorship data for id {} by {}", sponsorshipId, username);

        if (sponsorshipId == null) {
            throw new NotFoundException("Sponsorship id cannot be null");
        }

        Sponsorship sponsorship = sponsorshipRepository.findById(sponsorshipId)
                .orElseThrow(() -> new NotFoundException("Sponsorship not found"));

        return sponsorshipMapper.toDto(sponsorship);
    }
}
