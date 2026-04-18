package com.vts.hrms.controller;

import com.vts.hrms.dto.FeedbackDTO;
import com.vts.hrms.dto.SponsorshipDTO;
import com.vts.hrms.service.SponsorshipService;
import com.vts.hrms.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/sponsorship")
public class SponsorshipController {

    private final SponsorshipService sponsorshipService;

    public SponsorshipController(SponsorshipService sponsorshipService) {
        this.sponsorshipService = sponsorshipService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getSponsorshipList(@RequestParam String type, @RequestHeader String username)
    {
        List<SponsorshipDTO> list=sponsorshipService.getAllSponsorshipList(type,username);

        return ResponseEntity.ok(
            new ApiResponse(true, "Sponsorship list fetched", list)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addSponsorshipData(
            @Valid @RequestBody SponsorshipDTO dto,
            @RequestHeader String username)
    {
        SponsorshipDTO data = sponsorshipService.addSponsorshipData(dto, username);

        return ResponseEntity.ok(
                new ApiResponse(true, "Sponsorship data added successfully", data)
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse> editSponsorshipData(@Valid @RequestBody SponsorshipDTO dto, @RequestHeader String username) throws IOException {
        Optional<SponsorshipDTO> data = sponsorshipService.editSponsorshipData(dto,username);
        return ResponseEntity.ok(
                new ApiResponse(true, "Sponsorship data edited successfully", data)
        );
    }

    @GetMapping(value = "/{sponsorshipId}")
    public ResponseEntity<ApiResponse> GetSponsorshipDataByID(@PathVariable Long sponsorshipId, @RequestHeader String username) {
             SponsorshipDTO list = sponsorshipService.getSponsorshipById(sponsorshipId,username);
             return ResponseEntity.ok(
                new ApiResponse(true, "Sponsorship data fetched successfully", list)
        );
    }

}
