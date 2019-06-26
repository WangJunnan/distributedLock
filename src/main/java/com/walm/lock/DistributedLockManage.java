package com.walm.lock;

import com.walm.lock.redis.JedisConnectionFactory;
import com.walm.lock.redis.RedisConnectionFactory;
import com.walm.lock.redis.RedisDistributedLock;
import com.walm.lock.zk.ZkDistributedLock;

/**
 * <p>DistributedLockManage</p>
 *
 * @author wangjn
 * @date 2019/6/24
 */
public class DistributedLockManage {

    public static RedisConnectionFactory redisConnectionFactory;
    public static DistributedLock distributedLock;

    public static DistributedLock zkDistributedLock;

    private Integer count = 0;

    static {
        redisConnectionFactory = new JedisConnectionFactory("172.17.41.32", null, 6379);
        distributedLock = new RedisDistributedLock(redisConnectionFactory, "test:lock");
        zkDistributedLock = new ZkDistributedLock("172.17.41.32:2181");
    }

    public static final DistributedLockManage DISTRIBUTED_LOCK = new DistributedLockManage();

    public DistributedLock redisLock() {
        return distributedLock;
    }

    public static void main(String[] args) {
        DistributedLockManage distributedLockManage = new DistributedLockManage();
        Thread thread = new Thread(() -> {
            distributedLockManage.count();
        }, "test1");
        Thread thread2 = new Thread(() -> {
            distributedLockManage.count();
        }, "test2");
        Thread thread3 = new Thread(() -> {
            distributedLockManage.count();
        }, "test3");
        thread.start();
        thread2.start();
        thread3.start();
    }


    public void count() {
        for (int i = 0; i<50; i++) {
            if (zkDistributedLock.tryLock("count_lock")) {
                count = count + 1;
                System.out.println(count + " " + Thread.currentThread().getName());
            }
            zkDistributedLock.unlock("count_lock");
        }
    }
}
