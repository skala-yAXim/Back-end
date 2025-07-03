package com.yaxim.user.entity.validator;

import com.yaxim.user.entity.UserRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserRoleValidator implements ConstraintValidator<Enum, java.lang.Enum> {
    @Override
    public boolean isValid(java.lang.Enum value, ConstraintValidatorContext context) {
        if(value == UserRole.ADMIN){
            return false;
        }
        return value != null;
    }
}
