package com.github.ucluster.core.definition;

import com.github.ucluster.core.ActiveRecord;
import com.github.ucluster.core.User;

public interface PropertyValidator extends ActiveRecord.Property.Specification {

    ValidationResult validate(User user, String propertyPath);
}
