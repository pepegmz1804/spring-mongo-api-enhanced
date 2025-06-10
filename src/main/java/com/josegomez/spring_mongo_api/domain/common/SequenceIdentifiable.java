package com.josegomez.spring_mongo_api.domain.common;

// Used for make SequenceIdListener class generic and reusable
public interface SequenceIdentifiable {

    Long getId();

    void setId(Long id);

}
