package com.josegomez.spring_mongo_api.validation.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.josegomez.spring_mongo_api.validation.annotation.UniqueRoles;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Valid role array containig just a role of every type
public class UniqueRolesValidator implements ConstraintValidator<UniqueRoles, List<String>> {

    @Override
    public boolean isValid(List<String> roles, ConstraintValidatorContext context) {
        if (roles == null || roles.isEmpty()) {
            return true;
        }

        Set<String> uniqueKeys = new HashSet<>(roles);
        return uniqueKeys.size() == roles.size();
    }
}
