package com.josegomez.spring_mongo_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.josegomez.spring_mongo_api.domain.dto.ActivateAccountRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.AuthRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.AuthResponseDTO;
import com.josegomez.spring_mongo_api.domain.dto.GenerateTokenRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.GenerateTokenResponsetDTO;
import com.josegomez.spring_mongo_api.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/activate-account")
    public ResponseEntity<Void> activateAccount(@Valid @RequestBody ActivateAccountRequestDTO request) {
        authService.activateAccount(request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/start-activate-account")
    public ResponseEntity<GenerateTokenResponsetDTO> startActivateAccount(@RequestBody GenerateTokenRequestDTO request) {
        return ResponseEntity.ok(authService.startActivateAccount(request));
    }
}
