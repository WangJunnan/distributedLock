package com.walm.lock.redis;

import redis.clients.jedis.Jedis;

/**
 * <p>RedisConnectionFactory</p>
 *
 * @author wangjn
 * @date 2019/6/24
 */
public interface RedisConnectionFactory {

    Jedis getJedis();
}
