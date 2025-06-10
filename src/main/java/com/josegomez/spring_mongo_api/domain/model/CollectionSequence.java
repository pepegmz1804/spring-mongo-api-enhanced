package com.josegomez.spring_mongo_api.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity annotated for MongoDB. Used for save and retrieve next ids for collections
 */
@Document(collection = "collectionSequence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionSequence {

    @Id
    @Indexed(unique = true)
    private String collection;

    @Builder.Default
    private long current = 1;

}