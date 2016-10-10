package com.github.ucluster.mongo.api;


import static com.github.ucluster.mongo.api.PropertiesLoader.loadProperties;

public class Env {
    public static String getRedisHost() {
        return loadProperties("redis.properties").getProperty("redis.host");
    }

    public static int getRedisPort() {
        return Integer.valueOf(loadProperties("redis.properties").getProperty("redis.port"));
    }

    public static String getRedisPassword() {
        return loadProperties("redis.properties").getProperty("redis.password");
    }

    public static String getMongoHost() {
        return loadProperties("mongo.properties").getProperty("mongo.host");
    }

    public static int getMongoPort() {
        return Integer.valueOf(loadProperties("mongo.properties").getProperty("mongo.port"));
    }
}
