package com.walm.lock;

import java.util.concurrent.TimeUnit;

/**
 * <p>DistributedLock</p>
 *
 * @author wangjn
 * @date 2019/6/24
 */
public interface DistributedLock {

    /**
     * 获取锁，如果获取失败会尝试重新获取，但重试时间不会大于 waitTime
     *
     * @param lockKey
     * @param waitTime
     * @param expireTime
     * @param unit
     * @return
     */
    boolean lock(String lockKey, long waitTime, long expireTime, TimeUnit unit);

    /**
     * 获取锁
     *
     * @param lockKey
     * @return
     */
    boolean lock(String lockKey);

    /**
     * 尝试获取锁，立即返回成功或失败
     *
     * @param lockKey
     * @return
     */
    boolean tryLock(String lockKey, long expireTime, TimeUnit unit);

    /**
     * 尝试获取锁，立即返回成功或失败
     *
     * @param lockKey
     * @return
     */
    boolean tryLock(String lockKey);

    /**
     * 释放锁
     *
     * @param lockKey
     */
    void unlock(String lockKey);
}
