package com.github.ucluster.core;

public interface User extends Record {

    void authenticate(Property identityProperty, Property passwordProperty);

}
