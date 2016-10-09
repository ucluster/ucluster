package com.github.ucluster.session;

import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static com.github.ucluster.session.LogMessageTemplate.template;

public class Session {
    private static Logger LOGGER = LoggerFactory.getLogger(Session.class);

    private final Pool<Jedis> jedisPool;

    public Session(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }

    public List<Object> pipeline(Consumer<Pipeline> consumer) {
        try (Jedis jedis = jedisPool.getResource()) {
            final redis.clients.jedis.Pipeline pipelined = jedis.pipelined();
            final Pipeline wrapper = new Pipeline(pipelined);
            consumer.accept(wrapper);
            return pipelined.syncAndReturnAll();
        } catch (Exception e) {
            LOGGER.error("REDIS pipeline exception", e);
            throw new RuntimeException(e);
        }
    }

    public static class Pipeline {
        private redis.clients.jedis.Pipeline pipeline;

        public Pipeline(redis.clients.jedis.Pipeline pipelined) {
            pipeline = pipelined;
        }

        public void set(String key, Object value) {
            pipeline.set(key.getBytes(), Serializer.serializeObject(value));
        }

        public void expire(String key, int seconds) {
            pipeline.expire(key, seconds);
        }

        public void setex(String key, Object value, int seconds) {
            pipeline.setex(key.getBytes(), seconds, Serializer.serializeObject(value));
        }

        public void manualExpire(String group, String key, int seconds) {
            pipeline.zadd(group, new DateTime().getMillis() + seconds * 1000, key);
        }

        public void del(String key) {
            pipeline.del(key);
        }

        public void zrem(String key, String... members) {
            pipeline.zrem(key, members);
        }
    }

    public boolean set(String key, Object value) {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.set(key.getBytes(), Serializer.serializeObject(value));
            LOGGER.debug("REDIS set: key={}, value={}, result={}", key, value, result);

            return "OK".equals(result);
        } catch (Exception e) {
            LOGGER.error(template("REDIS set exception: key={}, value={}", key, value), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * hmset: only support one layer hash
     *
     * @param key
     * @param value
     * @return
     */
    public boolean hmset(String key, Map<String, String> value) {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.hmset(key, value);
            LOGGER.debug("REDIS hmset: key={}, value={}, result={}", key, value, result);

            return "OK".equals(result);
        } catch (Exception e) {
            LOGGER.error(template("REDIS hmset exception: key={}, value={}", key, value), e);
            throw new RuntimeException(e);
        }
    }

    public void hset(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            Long result = jedis.hset(key, field, value);
            LOGGER.debug("REDIS hset: key={}, field={}, value={}, result={}", key, field, value, result);
        } catch (Exception e) {
            LOGGER.error(template("REDIS hset exception: key={}, field={}, value={}", key, field, value), e);
            throw new RuntimeException(e);
        }
    }

    public Optional<String> hget(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.hget(key, field);
            LOGGER.debug("REDIS hset: key={}, field={}, value={}", key, field, result);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            LOGGER.error(template("REDIS hset exception: key={}, field={}", key, field), e);
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> hgetall(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> result = jedis.hgetAll(key);
            LOGGER.debug("REDIS hgetall: key={}, result={}", key, result);

            return result;
        } catch (Exception e) {
            LOGGER.error(template("REDIS hgetall exception: key={}", key), e);
            throw new RuntimeException(e);
        }
    }

    public boolean setex(String key, Object value, int expireSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            final String result = jedis.setex(key.getBytes(), expireSeconds, Serializer.serializeObject(value));
            LOGGER.debug("REDIS setex: key={}, value={}, result={}", key, value, result);

            return "OK".equals(result);
        } catch (Exception e) {
            LOGGER.error(template("REDIS setex exception: key={}, value={}", key, value), e);
            throw new RuntimeException(e);
        }
    }

    public Optional<Object> get(String key) {
        if (key == null || key.isEmpty()) {
            return Optional.empty();
        }
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] item = jedis.get(key.getBytes());

            if (null == item) {
                return Optional.empty();
            }

            Object value = Serializer.deserializeObject(item);
            LOGGER.debug("REDIS get: key={}, value={}", key, value);

            return Optional.of(value);
        } catch (Exception e) {
            LOGGER.error(template("REDIS get exception: key={}", key), e);
            return Optional.empty();
        }
    }

    public boolean del(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            final boolean result = jedis.del(key) > 0;
            LOGGER.debug("REDIS del: key={}", key, result);

            return result;
        } catch (Exception e) {
            LOGGER.error("REDIS del: key={}, exception=", key, e);
            return false;
        }
    }

    public void expire(String key, int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire(key, seconds);
            LOGGER.debug("REDIS expire: key={}, seconds={}", key, seconds);
        } catch (Exception e) {
            LOGGER.error(template("REDIS expire: key={}, seconds={}", key, seconds), e);
        }
    }

    public boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            final Boolean result = jedis.exists(key);
            LOGGER.debug("REDIS exists: key={}, result={}", key, result);

            return result;
        } catch (Exception e) {
            LOGGER.error(template("REDIS expire: key={}", key), e);
            return false;
        }
    }

    public void zadd(String key, double score, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            final Long result = jedis.zadd(key, score, member);
            LOGGER.debug("REDIS zadd: key={}, result={}", key, result);
        } catch (Exception e) {
            LOGGER.error(template("REDIS zadd: key={}", key), e);
        }
    }

    public double zscore(String key, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            final double result = jedis.zscore(key, member);
            LOGGER.debug("REDIS zscore: key={}, member={}, result={}", key, member, result);
            return result;
        } catch (Exception e) {
            LOGGER.error(template("REDIS zscore: key={}, member={}", key, member), e);
            throw new RuntimeException(e);
        }
    }

    public Set<String> zrangebyscore(String key, double min, double max) {
        try (Jedis jedis = jedisPool.getResource()) {
            final Set<String> members = jedis.zrangeByScore(key, min, max);
            LOGGER.debug("REDIS zrangebyscore: key={}, min={}, max={}, result={}", key, min, max, members);
            return members;
        } catch (Exception e) {
            LOGGER.error(template("REDIS zrangebyscore: key={}, min={}, max={}", key, min, max), e);
            return Sets.newHashSet();
        }
    }

    public long zrem(String key, String... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            final Long result = jedis.zrem(key, members);
            LOGGER.debug("REDIS zrem: key={}, members={}, result={}", key, members, result);
            return result;
        } catch (Exception e) {
            LOGGER.error(template("REDIS zrem: key={}, members={}", key, members), e);
            return 0;
        }
    }

    public long manualExpire(String group, String key, int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            final Long result = jedis.zadd(group, new DateTime().getMillis() + seconds * 1000, key);
            LOGGER.debug("REDIS manual expire: group={}, key={}, seconds={}, result={}", group, key, seconds, result);
            return result;
        } catch (Exception e) {
            LOGGER.error(template("REDIS manual expire: group={}, key={}, seconds={}", group, key, seconds), e);
            return 0;
        }
    }

    public static class Serializer {
        public static String serializeAsString(Object object) {
            return byteArrayToString(serializeObject(object));
        }

        public static String byteArrayToString(byte[] bytes) {
            return Arrays.toString(bytes);
        }

        public static Object deserializeFromString(String string) {
            return deserializeObject(stringToByteArray(string));
        }

        public static byte[] stringToByteArray(String string) {
            String[] byteValues = string.substring(1, string.length() - 1).split(",");
            byte[] bytes = new byte[byteValues.length];

            for (int i = 0, len = bytes.length; i < len; i++) {
                bytes[i] = Byte.parseByte(byteValues[i].trim());
            }

            return bytes;
        }

        public static byte[] serializeObject(Object obj) {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(256);
            Serializer serializer = new Serializer();
            try {
                serializer.serialize(obj, byteStream);
                return byteStream.toByteArray();
            } catch (Throwable e) {
                throw new RuntimeException("register thing: failed serialize object using " +
                        serializer.getClass().getSimpleName(), e);
            }
        }

        public static Object deserializeObject(byte[] b) {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(b);
            Serializer serializer = new Serializer();
            try {
                return serializer.deserialize(byteStream);
            } catch (Throwable ex) {
                throw new RuntimeException("register thing: failed to deserialize payload. is the byte array a result of corresponding serialization for " +
                        serializer.getClass().getSimpleName() + "?", ex);
            }
        }

        void serialize(Object object, OutputStream outputStream) throws IOException {
            if (!(object instanceof Serializable)) {
                throw new IllegalArgumentException("register thing: " + getClass().getSimpleName() + " requires a Serializable payload " +
                        "but received an object of type [" + object.getClass().getName() + "]");
            }
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        }

        Object deserialize(InputStream inputStream) throws IOException {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            try {
                return objectInputStream.readObject();
            } catch (ClassNotFoundException ex) {
                throw new IOException("register thing: failed to deserialize object type", ex);
            }
        }
    }
}
