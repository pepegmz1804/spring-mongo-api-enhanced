/**
 * The RoleController class in a Spring Boot application provides REST endpoints for CRUD operations
 * on roles with pagination and filtering capabilities.
 */
package com.josegomez.spring_mongo_api.controller;

import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.josegomez.spring_mongo_api.domain.common.swaggerAnnotations.RoleApiDoc;
import com.josegomez.spring_mongo_api.domain.dto.RoleRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.RoleResponseDTO;
import com.josegomez.spring_mongo_api.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * This Java class is a REST controller for managing roles with a specified API endpoint.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController implements RoleApiDoc {

        private final RoleService roleService;

        @Override
        @PostMapping
        public ResponseEntity<RoleResponseDTO> create(@Valid @RequestBody RoleRequestDTO role) {
                RoleResponseDTO roleResponse = roleService.save(role);
                URI location = URI.create("/api/roles/" + roleResponse.getId());
                return ResponseEntity.created(location).body(roleResponse);
        }

        @Override
        @PutMapping("/{id:\\d+}")
        public ResponseEntity<RoleResponseDTO> update(@PathVariable Long id,
                        @Valid @RequestBody RoleRequestDTO roleRequestDTO) {
                RoleResponseDTO updated = roleService.update(id, roleRequestDTO);
                return ResponseEntity.ok(updated);
        }

        @Override
        @GetMapping
        public ResponseEntity<Page<RoleResponseDTO>> getAll(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "asc") String direction,
                        @RequestParam(defaultValue = "false") boolean all) {
                Pageable pageable = PageRequest.of(page, size,
                                direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                                                : Sort.by(sortBy).ascending());

                Page<RoleResponseDTO> result = roleService.findAll(pageable, all);
                return ResponseEntity.ok(result);
        }

        @Override
        @GetMapping("/{id:\\d+}")
        public ResponseEntity<RoleResponseDTO> getById(@PathVariable Long id) {
                return roleService.findById(id).map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @Override
        @GetMapping("/filter")
        public ResponseEntity<Page<RoleResponseDTO>> searchByName(@RequestParam String name,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "asc") String direction) {
                Pageable pageable = PageRequest.of(page, size,
                                direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                                                : Sort.by(sortBy).ascending());

                Page<RoleResponseDTO> result = roleService.findByName(name, pageable);
                if (result.isEmpty()) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(result);
        }

        @Override
        @DeleteMapping("/{id:\\d+}")
        public ResponseEntity<Void> delete(@PathVariable Long id) {
                roleService.delete(id);
                return ResponseEntity.noContent().build();
        }
}
