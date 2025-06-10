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

/**
 * Service layer fore Role entity
 */
@Service
@RequiredArgsConstructor
@Validated
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private final ModelMapper modelMapper;
    private final Validator validator;

    /**
     * The `save` function maps a `RoleRequestDTO` to a `Role`, saves it using a repository, and
     * then maps the saved `Role` to a `RoleResponseDTO`.
     * 
     * @param roleRequest RoleRequestDTO object containing the data to be saved.
     * @return The `save` method returns a `RoleResponseDTO` object.
     */
    public RoleResponseDTO save(@Valid RoleRequestDTO roleRequest) {
        Role role = modelMapper.map(roleRequest, Role.class);
        Role savedRole = roleRepository.save(role);
        return modelMapper.map(savedRole, RoleResponseDTO.class);
    }

    /**
     * The `saveAll` method takes a list of `RoleRequestDTO` objects, validates them, maps them to
     * `Role` entities, saves them in the database, and returns a list of corresponding
     * `RoleResponseDTO` objects.
     * 
     * @param roleRequests The `saveAll` method you provided takes a list of `RoleRequestDTO`
     *        objects as input, which are represented by the `roleRequests` parameter. These objects
     *        are validated using a validator to check for any constraint violations. If there are
     *        no violations, the `RoleRequestDTO` objects are
     * @return The `saveAll` method returns a list of `RoleResponseDTO` objects.
     */
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

    /**
     * The `update` function updates a role entity with the provided data and returns a mapped
     * response DTO.
     * 
     * @param id The `id` parameter in the `update` method represents the unique identifier of the
     *        role that you want to update. It is used to retrieve the existing role from the
     *        database based on this identifier.
     * @param requestDTO The `requestDTO` parameter in the `update` method is of type
     *        `RoleRequestDTO`. It likely contains the updated information for a role, such as the
     *        name and key of the role that needs to be updated.
     * @return The `update` method returns a `RoleResponseDTO` object.
     */
    public RoleResponseDTO update(Long id, RoleRequestDTO requestDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Role not found with id: " + id));

        role.setName(requestDTO.getName());
        role.setKey(requestDTO.getKey());

        Role updated = roleRepository.save(role);
        return modelMapper.map(updated, RoleResponseDTO.class);
    }

    /**
     * The function `findAll` retrieves all roles or paginated roles from a repository and maps them
     * to `RoleResponseDTO` objects.
     * 
     * @param pageable `Pageable` is an interface in Spring Data that represents pagination
     *        information used for querying data in a paginated manner. It includes details such as
     *        the page number, page size, sorting criteria, etc. It allows you to easily retrieve a
     *        specific page of data from a larger dataset.
     * @param all The `all` parameter in the `findAll` method is a boolean flag that determines
     *        whether to fetch all roles or only a paginated list of roles. If `all` is set to
     *        `true`, the method will return all roles as a list without pagination. If `all` is set
     *        to
     * @return The method `findAll` returns a `Page` of `RoleResponseDTO`. If the `all` parameter is
     *         `true`, it returns all roles as a list in a `PageImpl`. If `all` is `false`, it
     *         returns roles based on the provided `Pageable` parameter from the `roleRepository`.
     */
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

    /**
     * This function finds a role by its ID and maps it to a RoleResponseDTO using ModelMapper,
     * returning an Optional.
     * 
     * @param id The `id` parameter is of type `Long` and it represents the unique identifier of the
     *        role that you want to find in the `roleRepository`. The `findById` method retrieves
     *        the role with the specified `id` from the repository and then maps it to a
     *        `RoleResponseDTO` using
     * @return An Optional object containing a RoleResponseDTO is being returned.
     */
    public Optional<RoleResponseDTO> findById(Long id) {
        return roleRepository.findById(id)
                .map(role -> modelMapper.map(role, RoleResponseDTO.class));
    }

    /**
     * This Java function finds roles by name and returns a page of RoleResponseDTO objects.
     * 
     * @param name The `name` parameter is a `String` representing the name to search for in the
     *        roles.
     * @param pageable Pageable is an interface in Spring Data that represents pagination
     *        information used in database queries. It contains details such as the page number,
     *        page size, sorting criteria, and more. This information is used to retrieve a specific
     *        page of results from a larger dataset.
     * @return A `Page` of `RoleResponseDTO` objects is being returned.
     */
    public Page<RoleResponseDTO> findByName(String name, Pageable pageable) {
        Page<Role> roles = roleRepository.findByNameContainingIgnoreCase(name, pageable);
        return roles.map(role -> modelMapper.map(role, RoleResponseDTO.class));
    }

    /**
     * The `delete` method deletes a role by its ID after checking if it is assigned to any users.
     * 
     * @param id The `delete` method takes a `Long` parameter `id`, which represents the unique
     *        identifier of the role that needs to be deleted. The method first checks if a role
     *        with the specified `id` exists in the `roleRepository`. If the role is not found, it
     *        throws a `ResponseStatus
     */
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

    /**
     * The `count()` function returns the total number of records in the role repository.
     * 
     * @return The count of roles from the role repository is being returned.
     */
    public long count() {
        return roleRepository.count();
    }
}
