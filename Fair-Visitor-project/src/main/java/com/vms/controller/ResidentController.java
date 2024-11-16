package com.vms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vms.dto.VisitResponseDto;
import com.vms.entity.User;
import com.vms.enums.VisitStatus;
import com.vms.service.ResidentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/resident")
@Tag(name = "Resident Controller", description = "Endpoints for managing resident-related activities.")
public class ResidentController {

    @Autowired
    private ResidentService residentService;

    @Operation(summary = "Act on a visit request", description = "Allows the resident to approve or reject a visit based on the provided visit ID and status.")
    @PutMapping("actOnVisit/{id}")
    public ResponseEntity<String> actOnVisit(@PathVariable Long id, @RequestParam VisitStatus visitStatus){
        return ResponseEntity.ok(residentService.updateVisit(id,visitStatus));
    }

    @Operation(summary = "Get pending visits", description = "Fetches a list of pending visits for the currently authenticated resident.")
    @GetMapping("/pendingVisits")
    public ResponseEntity<List<VisitResponseDto>> getPendingVisits(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(residentService.getPendingVisits(user.getId()));
    }
}
