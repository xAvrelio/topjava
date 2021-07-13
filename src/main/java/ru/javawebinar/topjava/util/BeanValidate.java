package ru.javawebinar.topjava.util;

import javax.validation.*;
import java.util.Set;

public class BeanValidate {

    private final Validator validator;
    private static BeanValidate instance;


    public static BeanValidate getInstance()
    {
        if (instance == null) instance = new BeanValidate();
        return instance;
    }

    private BeanValidate() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    public <T> void validate(T bean) {
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
    }

    public Validator getValidator() {
        return validator;
    }


}
