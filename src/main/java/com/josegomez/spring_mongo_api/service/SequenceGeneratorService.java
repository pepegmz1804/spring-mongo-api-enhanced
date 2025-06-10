package com.josegomez.spring_mongo_api.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.josegomez.spring_mongo_api.domain.model.CollectionSequence;

/**
 * This class  generates sequences retrieving CollectionSequence and using $inc.
 */
@Service
public class SequenceGeneratorService {

    @Autowired
    private MongoOperations mongoOperations;

    public long next(String collection) {
        CollectionSequence next = mongoOperations.findAndModify(
                new Query(Criteria.where("collection").is(collection)),
                new Update().inc("current", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                CollectionSequence.class);
        return Objects.requireNonNull(next).getCurrent();
    }
}