package com.github.ucluster.session;

import com.github.ucluster.session.junit.RedisTestRunner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;

@RunWith(RedisTestRunner.class)
public class SessionTest {
    @Inject
    Session session;

    @Test
    public void should_set() {
        assertThat(session.set("key", "value"), is(true));

        assertThat(session.get("key").get(), is("value"));
    }

    @Test
    public void should_able_to_set_email_as_key() {
        session.setex("confirm:kiwi.swhite.coder@gmail.com", "403125", 30);

        assertThat(session.get("confirm:kiwi.swhite.coder@gmail.com").get(), is("403125"));
    }

    @Test
    public void should_set_multi_times() {
        assertThat(session.set("key", "value"), is(true));
        assertThat(session.set("key", "value"), is(true));

        assertThat(session.get("key").get(), is("value"));
    }

    @Test
    public void should_hset() {
        session.hset("key", "field", "value");

        assertThat(session.hget("key", "field").get(), is("value"));
    }

    @Test
    public void should_hmset() {
        final Map<String, String> value = ImmutableMap.<String, String>builder().put("field1", "value1").put("field2", "value2").build();

        assertThat(session.hmset("key", value), is(true));
        assertThat(session.hgetall("key"), is(value));
    }

    @Test
    public void should_not_found() {
        assertThat(session.get("key").isPresent(), is(false));
    }

    @Test
    public void should_get_when_not_expire() {
        assertThat(session.setex("key", "value", 1), is(true));

        assertThat(session.get("key").get(), is("value"));
    }

    @Test
    public void should_not_found_when_expire() throws InterruptedException {
        assertThat(session.setex("key", "value", 1), is(true));

        Thread.sleep(1100);

        assertThat(session.get("key").isPresent(), is(false));
    }

    @Test
    public void should_expire() throws InterruptedException {
        session.hmset("key", ImmutableMap.<String, String>builder().put("field", "value").build());
        session.expire("key", 1);

        Thread.sleep(1100);

        assertThat(session.hgetall("key").isEmpty(), is(true));
    }

    @Test
    public void should_update_expire() throws InterruptedException {
        session.hmset("key", ImmutableMap.<String, String>builder().put("field", "value").build());
        session.expire("key", 1);
        session.expire("key", 100);

        Thread.sleep(1100);

        assertThat(session.hgetall("key").isEmpty(), is(false));
    }

    @Test
    public void should_key_exist() {
        session.hmset("key", ImmutableMap.<String, String>builder().put("field", "value").build());

        assertThat(session.exists("key"), is(true));
    }

    @Test
    public void should_key_not_exist() {
        assertThat(session.exists("key"), is(false));
    }

    @Test
    public void should_support_ranking() {
        session.zadd("key", ZonedDateTime.of(2016, 7, 10, 12, 0, 0, 0, ZoneId.systemDefault()).toEpochSecond(), "member1");
        session.zadd("key", ZonedDateTime.of(2016, 7, 10, 12, 0, 1, 0, ZoneId.systemDefault()).toEpochSecond(), "member2");
        session.zadd("key", ZonedDateTime.of(2016, 7, 10, 12, 0, 2, 0, ZoneId.systemDefault()).toEpochSecond(), "member3");

        final Set<String> members = session.zrangebyscore("key", 0, ZonedDateTime.of(2016, 7, 10, 12, 0, 1, 20, ZoneId.systemDefault()).toEpochSecond());

        assertThat(members, is(ImmutableSet.<String>builder().add("member1").add("member2").build()));
    }

    @Test
    public void should_support_remove_from_sorted_set() {
        session.zadd("key", ZonedDateTime.of(2016, 7, 10, 12, 0, 0, 0, ZoneId.systemDefault()).toEpochSecond(), "member1");
        session.zadd("key", ZonedDateTime.of(2016, 7, 10, 12, 0, 1, 0, ZoneId.systemDefault()).toEpochSecond(), "member2");
        session.zadd("key", ZonedDateTime.of(2016, 7, 10, 12, 0, 2, 0, ZoneId.systemDefault()).toEpochSecond(), "member3");

        session.zrem("key", "member3");

        final Set<String> members = session.zrangebyscore("key", 0, ZonedDateTime.of(2017, 7, 10, 12, 0, 1, 20, ZoneId.systemDefault()).toEpochSecond());
        assertThat(members, is(ImmutableSet.<String>builder().add("member1").add("member2").build()));
    }

    @Test
    public void should_update_score() {
        session.zadd("key", 1, "member");
        session.zadd("key", 2, "member");

        assertThat(session.zscore("key", "member"), is(closeTo(2, 1e-6)));
    }

    @Test
    public void should_manual_expire() {
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2016, 7, 10, 12, 0).getMillis());
        session.manualExpire("group", "key", 10);

        final double ttl = session.zscore("group", "key");
        assertThat(ttl, is(closeTo(new DateTime(2016, 7, 10, 12, 0, 10).getMillis(), 1e-6)));
    }

    @Test
    public void should_pipeline() {
        final List<Object> responses = session.pipeline($ -> {
            $.set("key", "value");
        });

        assertThat(responses.size(), is(1));
        assertThat(responses.get(0), is("OK"));

        assertThat(session.get("key").get(), is("value"));
    }

    @Test
    public void should_handle_complex_pipeline() {
        final List<Object> responses = session.pipeline($ -> {
            $.setex("thing", "command", 10);
            $.set("command_id", "command");
            $.expire("command_id", 10);
        });

        assertThat(responses.size(), is(3));

        assertThat(responses.get(0), is("OK"));
        assertThat(responses.get(1), is("OK"));
        assertThat(responses.get(2), is(1L));

        assertThat(session.get("thing").get(), is("command"));
        assertThat(session.get("command_id").get(), is("command"));
    }
}
