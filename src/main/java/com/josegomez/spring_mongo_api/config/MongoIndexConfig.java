package com.josegomez.spring_mongo_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import jakarta.annotation.PostConstruct;

/**
 * The `MongoIndexConfig` class in Java creates unique indexes on the "key" and "name" fields of the
 * "role" collection using `MongoTemplate`.
 */
@Configuration
public class MongoIndexConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        mongoTemplate.indexOps("role")
                .createIndex(new Index().on("key", Sort.Direction.ASC).unique());
        mongoTemplate.indexOps("role")
                .createIndex(new Index().on("name", Sort.Direction.ASC).unique());
    }
}
