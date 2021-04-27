package com.linkknown.iwork.util;

import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.executor.redis.RedisPoolFactory;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPool;

public class RedisUtil {

    public static boolean pingQueitly (String host, int port) {
        try {
            return ping(host, port);
        } catch (IWorkException e) {
            return false;
        }
    }

    public static boolean ping (String host, int port) throws IWorkException {
        JedisPool jedisPool = RedisPoolFactory.getInstance().getJedisPool(host, port);
        String ping = jedisPool.getResource().ping();
        if (!StringUtils.equalsIgnoreCase(ping, "PONG")) {
            throw new IWorkException(String.format("Redis 实例连接失败：host is %s, port is %d", host, port));
        }
        return true;
    }
}
