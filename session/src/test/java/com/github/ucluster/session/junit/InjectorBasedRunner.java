package com.github.ucluster.session.junit;

import com.github.ucluster.session.Session;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.ucluster.session.junit.RedisEnv.getRedisHost;
import static com.github.ucluster.session.junit.RedisEnv.getRedisPort;
import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;

public class InjectorBasedRunner extends BlockJUnit4ClassRunner {
    protected JedisPool jedisPool = new JedisPool(getRedisHost(), getRedisPort());
    protected Injector injector;

    public InjectorBasedRunner(Class<?> klass) throws InitializationError {
        super(klass);

        List<AbstractModule> modules = getAbstractModules();
        try {
            injector = createInjector(Modules.override(modules).with(new TestModule()));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        injector.injectMembers(this);
    }

    private List<AbstractModule> getAbstractModules() {
        List<AbstractModule> modules = new ArrayList<>(asList(new AbstractModule[]{
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Session.class).toInstance(new Session(jedisPool));
                    }
                }}));
        modules.addAll(getModules());
        return modules;
    }

    protected List<AbstractModule> getModules() {
        return Collections.emptyList();
    }

    @Override
    protected Object createTest() throws Exception {
        Object testClass = super.createTest();
        injector.injectMembers(testClass);
        return testClass;
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }
}
