package com.github.ucluster.session.junit;

import org.junit.rules.TestRule;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class RedisTestRunner extends InjectorBasedRunner {
    public RedisTestRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    private final TestRule clearRedis = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } finally {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.flushAll();
                }
            }
        }
    };

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> rules = new ArrayList<>();
        rules.add(clearRedis);
        rules.addAll(super.getTestRules(target));
        return rules;
    }
}