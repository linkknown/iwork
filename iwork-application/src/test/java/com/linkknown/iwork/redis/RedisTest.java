package com.linkknown.iwork.redis;

import com.linkknown.iwork.core.executor.redis.RedisPoolFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

@SpringBootTest
public class RedisTest {

    private Jedis jedis;

    @Before
    public void testBefore() {
        JedisPool jedisPool = RedisPoolFactory.getInstance().getJedisPool("localhost", 6379);
        jedis = jedisPool.getResource();
    }

    @Test
    public void testSetForString() throws InterruptedException {
        System.out.println(jedis.set("hello", "world"));

        System.out.println(jedis.get("hello"));

        System.out.println(jedis.exists("hello"));

        System.out.println(jedis.del("hello"));

        System.out.println(jedis.exists("hello"));

        System.out.println(jedis.mset("hello1", "world1", "hello2", "world2"));

        System.out.println(jedis.mget("hello1", "hello2"));

        System.out.println(jedis.setex("hello3", 1,"world3"));

        System.out.println(jedis.exists("hello3"));

        Thread.sleep(1001);

        System.out.println(jedis.exists("hello3"));

        System.out.println(jedis.setnx("hello4", "world4"));

        System.out.println(jedis.setnx("hello4", "world4"));
    }

    @Test
    public void testSetForIncrby() {
        System.out.println(jedis.set("number", "1"));

        System.out.println(jedis.incr("number"));

        System.out.println(jedis.get("number"));
    }

    /**
     * 使用的是压缩列表
     */
    @Test
    public void testForList () {
        System.out.println(jedis.rpush("list01", "a", "b", "c", "d", "e"));

        System.out.println(jedis.rpop("list01"));
        System.out.println(jedis.rpop("list01"));
        System.out.println(jedis.lpop("list01"));
        System.out.println(jedis.lpop("list01"));
        System.out.println(jedis.lpop("list01"));
    }

    @Test
    public void testForHash () {
        System.out.println(jedis.hset("map", "user", "zhangsan"));

        System.out.println(jedis.hset("map", "age", "20"));

        System.out.println(jedis.hgetAll("map"));
    }

    @Test
    public void testForSet () {
        System.out.println(jedis.sadd("set", "a", "b", "c", "d", "a", "b", "c", "d"));

        System.out.println(jedis.smembers("set"));
    }

    @Test
    public void testForZSet () {
        System.out.println(jedis.zadd("set1", 30, "tom"));
        System.out.println(jedis.zadd("set1", 10, "bob"));
        System.out.println(jedis.zadd("set1", 20, "smith"));

        System.out.println(jedis.zscore("set1", "tom"));
    }


    @Test
    public void testForHyperLogLog () {
        for (int i=0; i<100000; i++) {
            jedis.pfadd("account1", "user");
        }

        System.out.println(jedis.pfcount("account1"));
    }

    @After
    public void testAfter () {
        jedis.close();
    }
}
