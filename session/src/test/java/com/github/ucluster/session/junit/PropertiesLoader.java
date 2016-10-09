package com.github.ucluster.session.junit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    public static Properties loadProperties(String resourceFileName) {
        try {
            final InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourceFileName);
            final Properties properties = new Properties();
            properties.load(inputStream);

            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
