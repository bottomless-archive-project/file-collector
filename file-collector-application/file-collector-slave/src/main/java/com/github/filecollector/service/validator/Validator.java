package com.github.filecollector.service.validator;

import com.github.filecollector.service.domain.TargetLocation;

public interface Validator {

    boolean validate(TargetLocation targetLocation, String extension);

    boolean isValidatorFor(String extension);
}
