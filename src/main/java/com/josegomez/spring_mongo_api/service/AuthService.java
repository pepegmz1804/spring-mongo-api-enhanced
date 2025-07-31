package com.josegomez.spring_mongo_api.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.josegomez.spring_mongo_api.domain.dto.ActivateAccountRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.AuthRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.AuthResponseDTO;
import com.josegomez.spring_mongo_api.domain.dto.GenerateTokenRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.GenerateTokenResponsetDTO;
import com.josegomez.spring_mongo_api.domain.model.User;
import com.josegomez.spring_mongo_api.exceptions.ApiException;
import com.josegomez.spring_mongo_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDTO login(AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateAccessToken(userDetails);

        return new AuthResponseDTO(token);
    }

    public void activateAccount(ActivateAccountRequestDTO request) {

        Long id;
        try {
            id = jwtService.getIdFromActivationToken(request.getToken());
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Activation token invalid or expired");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Not found"));

        if (user.isEnabled()) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "User already activated");
        }

        user.setEnabled(true);
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

    }

    public GenerateTokenResponsetDTO startActivateAccount(GenerateTokenRequestDTO request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "User not found"));

        if (user.getPassword() != null && user.isEnabled()) {
            throw new ApiException(HttpStatus.CONFLICT.value(), "User already active");
        }

        String token = jwtService.generateActivationToken(user);

        // TODO: send email in the future

        return new GenerateTokenResponsetDTO(token);
    }
}
