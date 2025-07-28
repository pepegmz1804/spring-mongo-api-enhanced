package com.josegomez.spring_mongo_api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.josegomez.spring_mongo_api.domain.model.InitializationTask;

@Repository
public interface InitializationTaskRepository extends MongoRepository<InitializationTask, String> {}