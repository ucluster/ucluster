package com.github.ucluster.mongo.api.module;

import com.github.ucluster.mongo.api.Env;
import com.github.ucluster.session.Session;
import com.google.inject.AbstractModule;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;

public class SessionModule extends AbstractModule {
    private JedisPool jedisPool;

    @Override
    protected void configure() {
        bind(Session.class).toInstance(new Session(jedisPool()));
    }

    private Pool<Jedis> jedisPool() {
        if (jedisPool == null) {
            if (Env.getRedisPassword() != null) {
                jedisPool = new JedisPool(new GenericObjectPoolConfig(),
                        Env.getRedisHost(),
                        Env.getRedisPort(),
                        Protocol.DEFAULT_TIMEOUT,
                        Env.getRedisPassword());
            } else {
                jedisPool = new JedisPool(Env.getRedisHost(), Env.getRedisPort());
            }
        }
        return jedisPool;
    }
}
