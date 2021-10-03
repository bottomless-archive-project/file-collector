package com.github.collector.service.validator;

import com.github.collector.service.domain.TargetLocation;

public interface Validator {

    boolean validate(TargetLocation targetLocation, String extension);

    boolean isValidatorFor(String extension);
}
