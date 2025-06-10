package com.josegomez.spring_mongo_api.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import com.josegomez.spring_mongo_api.domain.dto.RoleRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.RoleResponseDTO;
import com.josegomez.spring_mongo_api.domain.model.Role;
import com.josegomez.spring_mongo_api.repository.RoleRepository;
import com.josegomez.spring_mongo_api.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Validated
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private final ModelMapper modelMapper;
    private final Validator validator;

    public RoleResponseDTO save(@Valid RoleRequestDTO roleRequest) {
        Role role = modelMapper.map(roleRequest, Role.class);
        Role savedRole = roleRepository.save(role);
        return modelMapper.map(savedRole, RoleResponseDTO.class);
    }

    public List<RoleResponseDTO> saveAll(List<RoleRequestDTO> roleRequests) {
        for (RoleRequestDTO dto : roleRequests) {
            Set<ConstraintViolation<RoleRequestDTO>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
        List<Role> roles =
                roleRequests.stream().map(dto -> modelMapper.map(dto, Role.class)).toList();
        List<Role> savedRoles = roleRepository.saveAll(roles);
        return savedRoles.stream().map(role -> modelMapper.map(role, RoleResponseDTO.class))
                .toList();
    }

    public RoleResponseDTO update(Long id, RoleRequestDTO requestDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Role not found with id: " + id));

        role.setName(requestDTO.getName());
        role.setKey(requestDTO.getKey());

        Role updated = roleRepository.save(role);
        return modelMapper.map(updated, RoleResponseDTO.class);
    }

    public Page<RoleResponseDTO> findAll(Pageable pageable, boolean all) {
        if (all) {
            List<RoleResponseDTO> allRoles = roleRepository.findAll().stream()
                    .map(role -> new RoleResponseDTO(role.getId(), role.getKey(), role.getName()))
                    .toList();

            return new PageImpl<>(allRoles, Pageable.unpaged(), allRoles.size());
        } else {
            return roleRepository.findAll(pageable)
                    .map(role -> new RoleResponseDTO(role.getId(), role.getKey(), role.getName()));
        }
    }

    public Optional<RoleResponseDTO> findById(Long id) {
        return roleRepository.findById(id)
                .map(role -> modelMapper.map(role, RoleResponseDTO.class));
    }

    public Page<RoleResponseDTO> findByName(String name, Pageable pageable) {
        Page<Role> roles = roleRepository.findByNameContainingIgnoreCase(name, pageable);
        return roles.map(role -> modelMapper.map(role, RoleResponseDTO.class));
    }

    public void delete(Long id) {
        roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Role not found with id: " + id));

        if (userRepository.existsByRolesContains(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete role. It is currently assigned to one or more users.");
        }

        roleRepository.deleteById(id);
    }

    public long count() {
        return roleRepository.count();
    }
}
