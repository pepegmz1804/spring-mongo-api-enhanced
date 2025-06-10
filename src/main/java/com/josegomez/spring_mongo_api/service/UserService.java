package com.josegomez.spring_mongo_api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.josegomez.spring_mongo_api.domain.dto.UserRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.UserResponseDTO;
import com.josegomez.spring_mongo_api.domain.model.Role;
import com.josegomez.spring_mongo_api.domain.model.User;
import com.josegomez.spring_mongo_api.repository.RoleRepository;
import com.josegomez.spring_mongo_api.repository.UserRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final Validator validator;

    public UserResponseDTO save(@Valid UserRequestDTO userRequest) {
        List<String> roleKeys = userRequest.getRoleKeys();

        List<Role> roles = roleRepository.findByKeyIn(roleKeys);

        if (roles.size() != roleKeys.size()) {
            Set<String> foundKeys = roles.stream()
                    .map(Role::getKey)
                    .collect(Collectors.toSet());

            Set<String> missingKeys = new HashSet<>(roleKeys);
            missingKeys.removeAll(foundKeys);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid role keys: " + String.join(", ", missingKeys));
        }

        User user = modelMapper.map(userRequest, User.class);
        List<Long> roleIds = roles.stream()
                .map(Role::getId)
                .toList();
        user.setRoles(roleIds);

        User savedUser = userRepository.save(user);

        MatchOperation match = Aggregation.match(Criteria.where("_id").is(savedUser.getId()));
        LookupOperation lookup = Aggregation.lookup("role", "roles", "_id", "roles");
        Aggregation aggregation = Aggregation.newAggregation(match, lookup);

        AggregationResults<UserResponseDTO> results = mongoTemplate.aggregate(aggregation, "user",
                UserResponseDTO.class);

        UserResponseDTO result = results.getUniqueMappedResult();
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve saved user with roles");
        }
        return result;
    }

    public List<UserResponseDTO> saveAll(List<UserRequestDTO> userRequests) {
        for (UserRequestDTO dto : userRequests) {
            Set<ConstraintViolation<UserRequestDTO>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
        List<User> users = userRequests.stream()
                .map(dto -> {
                    User user = modelMapper.map(dto, User.class);

                    List<Role> roles = roleRepository.findByKeyIn(dto.getRoleKeys());
                    if (roles.isEmpty()) {
                        throw new RuntimeException("User must have valid roles");
                    }
                    List<Long> roleIds = roles.stream()
                            .map(role -> role.getId())
                            .collect(Collectors.toList());
                    user.setRoles(roleIds);
                    return user;
                })
                .toList();
        List<User> savedUsers = userRepository.saveAll(users);
        return savedUsers.stream()
                .map(user -> modelMapper.map(user, UserResponseDTO.class))
                .toList();
    }

    public UserResponseDTO update(Long id, UserRequestDTO userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        List<String> roleKeys = userRequest.getRoleKeys();
        List<Role> roles = roleRepository.findByKeyIn(roleKeys);
        if (roles.size() != roleKeys.size()) {
            Set<String> foundKeys = roles.stream().map(Role::getKey).collect(Collectors.toSet());
            Set<String> missingKeys = new HashSet<>(roleKeys);
            missingKeys.removeAll(foundKeys);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid role keys: " + String.join(", ", missingKeys));
        }
        user.setFirstName(userRequest.getFirstName());
        user.setLastNamePaternal(userRequest.getLastNamePaternal());
        user.setLastNameMaternal(userRequest.getLastNameMaternal());
        List<Long> roleIds = roles.stream().map(Role::getId).toList();
        user.setRoles(roleIds);
        User updated = userRepository.save(user);

        MatchOperation match = Aggregation.match(Criteria.where("_id").is(updated.getId()));
        LookupOperation lookup = Aggregation.lookup("role", "roles", "_id", "roles");
        Aggregation aggregation = Aggregation.newAggregation(match, lookup);
        AggregationResults<UserResponseDTO> results = mongoTemplate.aggregate(aggregation, "user",
                UserResponseDTO.class);
        UserResponseDTO result = results.getUniqueMappedResult();

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve updated user with roles");
        }
        return result;
    }

    public Page<UserResponseDTO> findAll(Pageable pageable, boolean all) {
        if (all) {
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.lookup("role", "roles", "_id", "roles"));
            AggregationResults<UserResponseDTO> results = mongoTemplate.aggregate(aggregation, "user",
                    UserResponseDTO.class);
            List<UserResponseDTO> allUsers = results.getMappedResults();
            return new PageImpl<>(allUsers, Pageable.unpaged(), allUsers.size());
        } else {
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.lookup("role", "roles", "_id", "roles"));
            Sort sort = pageable.getSort();
            if (sort.isSorted()) {
                for (Sort.Order order : sort) {
                    String sortField = mapSortByField(order.getProperty());
                    operations.add(order.isAscending()
                            ? Aggregation.sort(Sort.Direction.ASC, sortField)
                            : Aggregation.sort(Sort.Direction.DESC, sortField));
                }
            }
            operations.add(Aggregation.skip(pageable.getOffset()));
            operations.add(Aggregation.limit(pageable.getPageSize()));

            Aggregation aggregation = Aggregation.newAggregation(operations);
            AggregationResults<UserResponseDTO> results = mongoTemplate.aggregate(aggregation, "user",
                    UserResponseDTO.class);
            List<UserResponseDTO> usersPage = results.getMappedResults();
            long total = mongoTemplate.count(new Query(), "user");
            return new PageImpl<>(usersPage, pageable, total);
        }
    }

    public Optional<UserResponseDTO> findByIdWithRoles(Long userId) {
        MatchOperation match = Aggregation.match(Criteria.where("_id").is(userId));
        Aggregation aggregation = Aggregation.newAggregation(
                match,
                Aggregation.lookup("role", "roles", "_id", "roles"));
        AggregationResults<UserResponseDTO> results = mongoTemplate.aggregate(aggregation, "user",
                UserResponseDTO.class);
        return Optional.ofNullable(results.getUniqueMappedResult());
    }

    public Page<UserResponseDTO> findByName(String name, Pageable pageable) {
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria nameCriteria = Criteria.where("firstName").regex(name, "i");
        operations.add(Aggregation.match(nameCriteria));
        operations.add(Aggregation.lookup("role", "roles", "_id", "roles"));
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            for (Sort.Order order : sort) {
                String sortField = mapSortByField(order.getProperty());
                operations.add(order.isAscending()
                        ? Aggregation.sort(Sort.Direction.ASC, sortField)
                        : Aggregation.sort(Sort.Direction.DESC, sortField));
            }
        }
        operations.add(Aggregation.skip(pageable.getOffset()));
        operations.add(Aggregation.limit(pageable.getPageSize()));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<UserResponseDTO> results = mongoTemplate.aggregate(aggregation, "user",
                UserResponseDTO.class);
        List<UserResponseDTO> usersPage = results.getMappedResults();
        Criteria countCriteria = Criteria.where("firstName").regex(name, "i");
        long total = mongoTemplate.count(new Query(countCriteria), "user");
        return new PageImpl<>(usersPage, pageable, total);
    }

    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        userRepository.deleteById(id);
    }

    public long count() {
        return userRepository.count();
    }

    private String mapSortByField(String sortBy) {
        switch (sortBy) {
            case "id":
                return "_id";
            case "firstName":
                return "firstName";
            case "lastNamePaternal":
                return "lastNamePaternal";
            case "lastNameMaternal":
                return "lastNameMaternal";
            default:
                return sortBy;
        }
    }

}
