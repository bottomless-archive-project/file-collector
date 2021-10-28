package com.github.filecollector.service.validator;

import java.io.InputStream;

public interface Validator {

    boolean validate(InputStream validationTarget, String extension);

    boolean isValidatorFor(String extension);
}
