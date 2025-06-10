package com.josegomez.spring_mongo_api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.josegomez.spring_mongo_api.domain.model.Role;

@Repository
public interface RoleRepository extends MongoRepository<Role, Long> {

    List<Role> findByKeyIn(Collection<String> keys);

    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);


}
