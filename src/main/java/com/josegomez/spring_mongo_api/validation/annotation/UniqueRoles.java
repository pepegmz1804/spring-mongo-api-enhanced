package com.josegomez.spring_mongo_api.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.josegomez.spring_mongo_api.validation.validator.UniqueRolesValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = UniqueRolesValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueRoles {

    String message() default "Roles must be unique";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}