package com.github.ucluster.core;

public interface User extends ActiveRecord {

    void authenticate(Property identityProperty, Property passwordProperty);

}
