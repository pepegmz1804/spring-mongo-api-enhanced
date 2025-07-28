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

/**
 * Service layer fore Role entity
 */
@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final Validator validator;

    /**
     * The `save` method in Java validates and saves a user with specified roles, performing role
     * key validation and retrieving the saved user with roles using MongoDB aggregation.
     * 
     * @param userRequest The `save` method you provided seems to be responsible for saving a user
     *        along with their roles in a database. It performs validation on the role keys provided
     *        in the `UserRequestDTO`, checks if all the role keys are valid, saves the user with
     *        the corresponding roles, and then retrieves the saved
     * @return The `save` method returns a `UserResponseDTO` object.
     */
    public UserResponseDTO save(@Valid UserRequestDTO userRequest) {
        List<String> roleKeys = userRequest.getRoleKeys();

        List<Role> roles = roleRepository.findByKeyIn(roleKeys);

        if (roles.size() != roleKeys.size()) {
            Set<String> foundKeys = roles.stream().map(Role::getKey).collect(Collectors.toSet());

            Set<String> missingKeys = new HashSet<>(roleKeys);
            missingKeys.removeAll(foundKeys);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid role keys: " + String.join(", ", missingKeys));
        }

        User user = modelMapper.map(userRequest, User.class);
        List<Long> roleIds = roles.stream().map(Role::getId).toList();
        user.setRoles(roleIds);
        user.setEnabled(false); // every user created has enabled false until the aacount activate
        
        User savedUser = userRepository.save(user);

        MatchOperation match = Aggregation.match(Criteria.where("_id").is(savedUser.getId()));
        LookupOperation lookup = Aggregation.lookup("role", "roles", "_id", "roles");
        Aggregation aggregation = Aggregation.newAggregation(match, lookup);

        AggregationResults<UserResponseDTO> results =
                mongoTemplate.aggregate(aggregation, "user", UserResponseDTO.class);

        UserResponseDTO result = results.getUniqueMappedResult();
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve saved user with roles");
        }
        return result;
    }

    /**
     * The function `saveAll` validates a list of user requests, maps them to User entities, assigns
     * roles to users, saves users to the database, and returns a list of UserResponseDTO objects.
     * 
     * @param userRequests The `saveAll` method you provided takes a list of `UserRequestDTO`
     *        objects as input. It then validates each `UserRequestDTO` object using a validator and
     *        throws a `ConstraintViolationException` if any violations are found.
     * @return The method `saveAll` returns a list of `UserResponseDTO` objects, which are created
     *         by mapping the saved `User` entities to `UserResponseDTO` using a `ModelMapper`.
     */
    public List<UserResponseDTO> saveAll(List<UserRequestDTO> userRequests) {
        for (UserRequestDTO dto : userRequests) {
            Set<ConstraintViolation<UserRequestDTO>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
        List<User> users = userRequests.stream().map(dto -> {
            User user = modelMapper.map(dto, User.class);

            List<Role> roles = roleRepository.findByKeyIn(dto.getRoleKeys());
            if (roles.isEmpty()) {
                throw new RuntimeException("User must have valid roles");
            }
            List<Long> roleIds =
                    roles.stream().map(role -> role.getId()).collect(Collectors.toList());
            user.setRoles(roleIds);
            user.setEnabled(false); // every user created has enabled false until the aacount activate
            return user;
        }).toList();
        List<User> savedUsers = userRepository.saveAll(users);
        return savedUsers.stream().map(user -> modelMapper.map(user, UserResponseDTO.class))
                .toList();
    }

    /**
     * The `update` function in Java updates a user's information and roles in a database,
     * performing validation checks and returning the updated user with roles.
     * 
     * @param id The `id` parameter in the `update` method represents the unique identifier of the
     *        user that you want to update in the database. This identifier is typically used to
     *        locate the specific user record that needs to be modified.
     * @param userRequest The `update` method you provided is responsible for updating a user's
     *        information and roles in the database. It first retrieves the user by the given `id`,
     *        then validates and updates the roles based on the `roleKeys` provided in the
     *        `userRequestDTO`.
     * @return The `update` method returns a `UserResponseDTO` object, which represents the updated
     *         user with roles after the update operation is performed.
     */
    public UserResponseDTO update(Long id, UserRequestDTO userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with id: " + id));
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
        AggregationResults<UserResponseDTO> results =
                mongoTemplate.aggregate(aggregation, "user", UserResponseDTO.class);
        UserResponseDTO result = results.getUniqueMappedResult();

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve updated user with roles");
        }
        return result;
    }

    /**
     * The `findAll` method retrieves either all users or a paginated list of users with roles using
     * MongoDB aggregation.
     * 
     * @param pageable The `pageable` parameter in the `findAll` method is used for pagination in
     *        the query results. It contains information about the requested page, such as the page
     *        size, page number, sorting criteria, etc. This allows the method to retrieve a
     *        specific subset of results from the database based on the
     * @param all The `all` parameter in the `findAll` method is a boolean flag that determines
     *        whether to fetch all users without pagination or to apply pagination and return a
     *        specific page of users.
     * @return The `findAll` method returns a `Page` of `UserResponseDTO` objects. If the `all`
     *         parameter is `true`, it retrieves all users from the database without pagination. If
     *         `all` is `false`, it applies pagination based on the `Pageable` parameter and returns
     *         a paginated result set of users.
     */
    public Page<UserResponseDTO> findAll(Pageable pageable, boolean all) {
        if (all) {
            Aggregation aggregation =
                    Aggregation.newAggregation(Aggregation.lookup("role", "roles", "_id", "roles"));
            AggregationResults<UserResponseDTO> results =
                    mongoTemplate.aggregate(aggregation, "user", UserResponseDTO.class);
            List<UserResponseDTO> allUsers = results.getMappedResults();
            return new PageImpl<>(allUsers, Pageable.unpaged(), allUsers.size());
        } else {
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.lookup("role", "roles", "_id", "roles"));
            Sort sort = pageable.getSort();
            if (sort.isSorted()) {
                for (Sort.Order order : sort) {
                    String sortField = mapSortByField(order.getProperty());
                    operations.add(
                            order.isAscending() ? Aggregation.sort(Sort.Direction.ASC, sortField)
                                    : Aggregation.sort(Sort.Direction.DESC, sortField));
                }
            }
            operations.add(Aggregation.skip(pageable.getOffset()));
            operations.add(Aggregation.limit(pageable.getPageSize()));

            Aggregation aggregation = Aggregation.newAggregation(operations);
            AggregationResults<UserResponseDTO> results =
                    mongoTemplate.aggregate(aggregation, "user", UserResponseDTO.class);
            List<UserResponseDTO> usersPage = results.getMappedResults();
            long total = mongoTemplate.count(new Query(), "user");
            return new PageImpl<>(usersPage, pageable, total);
        }
    }

    /**
     * This Java function finds a user by their ID along with their roles using MongoDB aggregation.
     * 
     * @param userId The `userId` parameter is the unique identifier of the user for which you want
     *        to find information along with their roles.
     * @return The method `findByIdWithRoles` returns an `Optional` containing a `UserResponseDTO`
     *         object, which represents a user with their associated roles.
     */
    public Optional<UserResponseDTO> findByIdWithRoles(Long userId) {
        MatchOperation match = Aggregation.match(Criteria.where("_id").is(userId));
        Aggregation aggregation = Aggregation.newAggregation(match,
                Aggregation.lookup("role", "roles", "_id", "roles"));
        AggregationResults<UserResponseDTO> results =
                mongoTemplate.aggregate(aggregation, "user", UserResponseDTO.class);
        return Optional.ofNullable(results.getUniqueMappedResult());
    }

    /**
     * This Java function searches for users by name, applies sorting and pagination, and returns
     * the results as a Page of UserResponseDTO objects.
     * 
     * @param name The `name` parameter in the `findByName` method is used to search for users by
     *        their first name. The method performs a case-insensitive regex search on the
     *        `firstName` field in the database to find users whose first name matches the provided
     *        `name` value.
     * @param pageable The `pageable` parameter in the `findByName` method is used for pagination in
     *        the query results. It contains information about the page size, current page number,
     *        sorting criteria, and more. This allows the method to retrieve a specific page of
     *        results from the database based on the provided criteria.
     * @return This method returns a Page of UserResponseDTO objects that match the given name, with
     *         pagination handled by the Pageable parameter. The method performs an aggregation
     *         query on a MongoDB collection named "user" to find users by their first name
     *         (case-insensitive regex match), applies sorting based on the provided pageable
     *         sorting criteria, skips the specified number of records based on the page offset,
     *         limits the number of results
     */
    public Page<UserResponseDTO> findByName(String name, Pageable pageable) {
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria nameCriteria = Criteria.where("firstName").regex(name, "i");
        operations.add(Aggregation.match(nameCriteria));
        operations.add(Aggregation.lookup("role", "roles", "_id", "roles"));
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            for (Sort.Order order : sort) {
                String sortField = mapSortByField(order.getProperty());
                operations.add(order.isAscending() ? Aggregation.sort(Sort.Direction.ASC, sortField)
                        : Aggregation.sort(Sort.Direction.DESC, sortField));
            }
        }
        operations.add(Aggregation.skip(pageable.getOffset()));
        operations.add(Aggregation.limit(pageable.getPageSize()));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<UserResponseDTO> results =
                mongoTemplate.aggregate(aggregation, "user", UserResponseDTO.class);
        List<UserResponseDTO> usersPage = results.getMappedResults();
        Criteria countCriteria = Criteria.where("firstName").regex(name, "i");
        long total = mongoTemplate.count(new Query(countCriteria), "user");
        return new PageImpl<>(usersPage, pageable, total);
    }

    /**
     * The `delete` function deletes a user by their ID from the repository after checking if the
     * user exists.
     * 
     * @param id The `id` parameter in the `delete` method represents the unique identifier of the
     *        user that you want to delete from the repository.
     */
    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with id: " + id));
        userRepository.deleteById(id);
    }

    /**
     * The `count()` function returns the total number of entities in the user repository.
     * 
     * @return The count of entities in the user repository is being returned.
     */
    public long count() {
        return userRepository.count();
    }

    /**
     * The function `mapSortByField` maps input strings to corresponding field names for sorting in
     * a Java program.
     * 
     * @param sortBy The `mapSortByField` method takes a `sortBy` parameter as input, which is a
     *        string representing the field by which the data should be sorted. The method then maps
     *        this input to a corresponding field name that is used for sorting in the data
     *        structure. The method returns the mapped field name
     * @return The method `mapSortByField` is returning the corresponding field name based on the
     *         input `sortBy` parameter. If the `sortBy` parameter matches one of the cases ("id",
     *         "firstName", "lastNamePaternal", "lastNameMaternal"), then the corresponding field
     *         name is returned. If the `sortBy` parameter does not match any of the cases, then the
     *         `sortBy` parameter itself is
     */
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
