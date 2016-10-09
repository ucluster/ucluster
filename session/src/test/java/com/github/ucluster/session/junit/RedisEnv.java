package com.github.ucluster.session.junit;

import static com.github.ucluster.session.junit.PropertiesLoader.loadProperties;

public class RedisEnv {
    public static String getRedisHost() {
        return loadProperties("redis.properties").getProperty("redis.host");
    }

    public static int getRedisPort() {
        return Integer.valueOf(loadProperties("redis.properties").getProperty("redis.port"));
    }

    public static String getRedisPassword() {
        return loadProperties("redis.properties").getProperty("redis.password");
    }
}
