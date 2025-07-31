package com.josegomez.spring_mongo_api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.josegomez.spring_mongo_api.domain.model.User;

// Define a Spring Data MongoDB repository interface for managing `User` entities.
@Repository
public interface UserRepository extends MongoRepository<User, Long> {

    /**
     * This function retrieves a page of users whose first name contains a specified
     * case-insensitive substring.
     * 
     * @param name     The `name` parameter is a string that is used to search for
     *                 users whose first
     *                 name contains the specified value. The search is
     *                 case-insensitive, meaning that it
     *                 will match names regardless of the case of the letters.
     * @param pageable The `pageable` parameter in the
     *                 `findByFirstNameContainingIgnoreCase` method
     *                 is used for pagination. It allows you to specify the page
     *                 number, the number of items
     *                 per page, sorting options, and more. This helps in retrieving
     *                 a subset of results from
     *                 a larger dataset, making it easier to manage
     * @return A Page of User objects whose first name contains the specified name
     *         (case-insensitive), with pagination handled by the Pageable
     *         parameter.
     */
    Page<User> findByFirstNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * This function checks if a role with a specific ID exists in a collection of
     * roles.
     * 
     * @param roleId The `roleId` parameter is a unique identifier for a specific
     *               role in a system.
     *               The method `existsByRolesContains(Long roleId)` is likely
     *               checking if there is an
     *               entity that contains the specified role in its roles collection
     *               or attribute.
     * @return This method is returning a boolean value, which indicates whether
     *         there is an entity
     *         that contains the specified roleId in its roles collection.
     */
    boolean existsByRolesContains(Long roleId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

}
