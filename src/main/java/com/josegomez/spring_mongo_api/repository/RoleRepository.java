package com.josegomez.spring_mongo_api.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.josegomez.spring_mongo_api.domain.model.Role;

// Define a Spring Data MongoDB repository interface for managing `Role` entities.
@Repository
public interface RoleRepository extends MongoRepository<Role, Long> {

    /**
     * This function retrieves a list of Role objects based on a collection of key strings.
     * 
     * @param keys The `keys` parameter is a collection of strings that are used as search criteria
     *        to find roles in the system. The `findByKeyIn` method will search for roles whose key
     *        matches any of the strings in the provided collection of keys.
     * @return This method is returning a list of Role objects that have keys matching the strings
     *         in the provided collection.
     */
    List<Role> findByKeyIn(Collection<String> keys);

    /**
     * This function searches for roles by name with case-insensitive matching and returns a
     * paginated result.
     * 
     * @param name The `name` parameter is a string that is used to search for roles by name. The
     *        search is case-insensitive and can match partial names.
     * @param pageable The `pageable` parameter in the `findByNameContainingIgnoreCase` method is
     *        used for pagination. It allows you to specify the page number, the number of items per
     *        page, sorting options, and other pagination-related information when querying the
     *        database. This helps in retrieving data in chunks or pages, making
     * @return A Page of Role entities that have names containing the specified name
     *         (case-insensitive), with pagination applied according to the provided Pageable
     *         object.
     */
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);


}
