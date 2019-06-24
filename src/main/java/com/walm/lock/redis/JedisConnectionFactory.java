package com.walm.lock.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * <p>JedisConnectionFactory</p>
 *
 * @author wangjn
 * @date 2019/6/24
 */
public class JedisConnectionFactory implements RedisConnectionFactory {

    private String host;
    private int port;
    private String password;

    private JedisPool jedisPool;

    public JedisConnectionFactory(String host, String password, int port) {
        this.host = host;
        this.password = password;
        this.port = port;
        init();
    }

    public void init() {
        jedisPool = new JedisPool(new JedisPoolConfig(), host, port, 2000, password);
    }

    @Override
    public Jedis getJedis() {
        return jedisPool.getResource();
    }
}
