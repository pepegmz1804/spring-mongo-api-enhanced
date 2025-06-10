package com.josegomez.spring_mongo_api.domain.common;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;


/**
 * The `DtoMapperUtil` class is a Spring component that utilizes ModelMapper for mapping objects
 * between different classes.
 */
@Component
@RequiredArgsConstructor
public class DtoMapperUtil {

    private final ModelMapper modelMapper;

    public <S, T> T map(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }
}
