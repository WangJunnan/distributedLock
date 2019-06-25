package com.walm.lock.redis;

import com.walm.lock.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>RedisDistributedLock</p>
 *
 * @author wangjn
 * @date 2019/6/24
 */
@Slf4j
public class RedisDistributedLock implements DistributedLock {

    private RedisConnectionFactory redisConnectionFactory;
    private String lockKeyPrefix;

    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "EX";

    public static ThreadLocal<String> THREADLOCAL_LOCKVALUE = new ThreadLocal<>();

    public RedisDistributedLock(RedisConnectionFactory redisConnectionFactory, String lockKeyPrefix) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.lockKeyPrefix = lockKeyPrefix;
    }

    @Override
    public boolean lock(String lockKey, long waitTime, long expireTime, TimeUnit unit) {
        // 支持可重入
        if (THREADLOCAL_LOCKVALUE.get() != null) {
            return true;
        }
        Jedis jedis = redisConnectionFactory.getJedis();
        long startLockTime = System.currentTimeMillis();
        try {
            for (; ; ) {
                String value = UUID.randomUUID().toString();
                String result = jedis.set(buildKey(lockKey), value, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, unit.toSeconds(expireTime));
                if ("OK".equalsIgnoreCase(result)) {
                    THREADLOCAL_LOCKVALUE.set(value);
                    return true;
                }
                Thread.sleep(200);
                // get lock fail
                if (System.currentTimeMillis() - startLockTime >= unit.toMillis(waitTime)) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("error_RedisDistributedLock_tryLock", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    @Override
    public boolean tryLock(String lockKey, long expireTime, TimeUnit unit) {
        if (THREADLOCAL_LOCKVALUE.get() != null) {
            return true;
        }
        Jedis jedis = redisConnectionFactory.getJedis();
        try {
            String value = UUID.randomUUID().toString();
            String result = jedis.set(buildKey(lockKey), value, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, unit.toSeconds(expireTime));
            if ("OK".equalsIgnoreCase(result)) {
                THREADLOCAL_LOCKVALUE.set(value);
                return true;
            }
        } catch (Exception e) {
            log.error("error_RedisDistributedLock_lock", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    @Override
    public void unlock(String lockKey) {
        Jedis jedis = redisConnectionFactory.getJedis();
        try {
            final String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            List<String> keys = Collections.singletonList(buildKey(lockKey));
            String value = THREADLOCAL_LOCKVALUE.get();
            if (value == null) {
                return;
            }
            List<String> args = Collections.singletonList(value);
            if (!Long.valueOf(1).equals(jedis.eval(luaScript, keys, args))) {
                log.error("unlock fail, it could be that the lock has expired");
            }
        } catch (Exception e) {
            log.error("error_RedisDistributedLock_unlock", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
            THREADLOCAL_LOCKVALUE.remove();
        }
    }

    private String buildKey(String key) {
        return lockKeyPrefix + key;
    }
}
