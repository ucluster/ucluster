package com.github.ucluster.mongo.api;

import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {
    private Properties getProperties(String propertyFilename) {
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream(propertyFilename));
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return properties;
    }

    public static Properties loadProperties(String propertyFilename) {
        return new PropertiesLoader().getProperties(propertyFilename);
    }
}
