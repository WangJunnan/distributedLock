package com.walm.lock;

import com.walm.lock.redis.JedisConnectionFactory;
import com.walm.lock.redis.RedisConnectionFactory;
import com.walm.lock.redis.RedisDistributedLock;

import java.util.concurrent.TimeUnit;

/**
 * <p>DistributedLockManage</p>
 *
 * @author wangjn
 * @date 2019/6/24
 */
public class DistributedLockManage {
    public static RedisConnectionFactory redisConnectionFactory;
    public static DistributedLock distributedLock;

    private Integer count = 0;

    static {
        redisConnectionFactory = new JedisConnectionFactory("172.17.41.32", null, 6379);
        distributedLock = new RedisDistributedLock(redisConnectionFactory, "test:lock");
    }

    public static final DistributedLockManage DISTRIBUTED_LOCK = new DistributedLockManage();

    public DistributedLock redisLock() {
        return distributedLock;
    }

    public static void main(String[] args) {
        DistributedLockManage distributedLockManage = new DistributedLockManage();
        Thread thread = new Thread(() -> {
            distributedLockManage.count();
        });
        Thread thread2 = new Thread(() -> {
            distributedLockManage.count();
        });
        Thread thread3 = new Thread(() -> {
            distributedLockManage.count();
        });
        thread.start();
        thread2.start();
        thread3.start();
    }


    public void count() {
            if (DISTRIBUTED_LOCK.redisLock().tryLock("count_lock_1",  5, TimeUnit.SECONDS)) {
                count = count + 1;
                System.out.println(count + " " + Thread.currentThread().getName());
                distributedLock.unlock("count_lock_1");
            }
    }
}
