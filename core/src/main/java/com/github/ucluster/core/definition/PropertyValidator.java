package com.github.ucluster.core.definition;

import com.github.ucluster.core.User;

public interface PropertyValidator extends User.Property.Specification {

    ValidationResult validate(User user, String propertyPath);
}
