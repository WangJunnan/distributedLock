package com.walm.lock.zk;

import com.walm.lock.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <p>ZkDistributedLock</p>
 * <p>
 * <p>基于zk临时有序节点实现的分布式锁，是公平锁的一种</p>
 *
 * @author wangjn
 * @date 2019/6/25
 */
@Slf4j
public class ZkDistributedLock implements DistributedLock {

    private ZooKeeper zkCli;

    private CountDownLatch connectLatch = new CountDownLatch(1);

    public static final String rootLockPath = "/walm_locks";

    private ThreadLocal<String> currentLockNode = new ThreadLocal<>();

    public ZkDistributedLock(String connectStr) {
        try {
            zkCli = new ZooKeeper(connectStr, 20000, (watchedEvent) -> {
                if (Watcher.Event.KeeperState.SyncConnected.equals(watchedEvent.getState())) {
                    // 连接成功
                    log.info("connect zk success ...");
                    connectLatch.countDown();
                }
            });
            connectLatch.await();
            Stat stat = zkCli.exists(rootLockPath, false);
            if (stat == null) {
                zkCli.create(rootLockPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (InterruptedException e) {
            log.error("error_zkDistributedLock_await_", e);
        } catch (IOException | KeeperException e) {
            log.error("error_zkDistributedLock_await_io", e);
        }
    }


    @Override
    public boolean lock(String lockKey, long waitTime, long expireTime, TimeUnit unit) {
        try {
            // 创建临时有序节点
            String nowNode = zkCli.create(rootLockPath + "/" + lockKey, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            List<String> childrenPaths = zkCli.getChildren(rootLockPath, false);
            // 排序
            TreeSet<String> sortedNodes = new TreeSet<>();
            for (String node : childrenPaths) {
                sortedNodes.add(rootLockPath + "/" + node);
            }
            if (nowNode.equals(sortedNodes.first())) {
                currentLockNode.set(nowNode);
                return true;
            }
            String previousNode = sortedNodes.lower(nowNode);
            CountDownLatch nodeDLatch = new CountDownLatch(1);

            // 不存在，说明前一个节点已经释放锁
            if (null == zkCli.exists(previousNode, new LockWatcher(nodeDLatch))) {
                currentLockNode.set(nowNode);
                return true;
            }
            if (connectLatch.await(waitTime, unit)) {
                currentLockNode.set(nowNode);
                return true;
            }
            zkCli.delete(nowNode, -1);
        } catch (KeeperException e) {
            log.error("error_zkDistributedLock_lock", e);
        } catch (InterruptedException e) {
            log.error("error_zkDistributedLock_lock", e);
        }
        return false;
    }

    @Override
    public boolean lock(String lockKey) {
        return lock(lockKey, 5, 0, TimeUnit.SECONDS);
    }

    @Override
    public boolean tryLock(String lockKey, long expireTime, TimeUnit unit) {
        // 创建临时有序节点
        String nowNode = null;
        try {
            nowNode = zkCli.create(rootLockPath + "/" + lockKey, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            List<String> childrenPaths = zkCli.getChildren(rootLockPath, false);
            // 排序
            TreeSet<String> sortedNodes = new TreeSet<>();
            for (String node : childrenPaths) {
                sortedNodes.add(rootLockPath + "/" + node);
            }
            if (nowNode.equals(sortedNodes.first())) {
                currentLockNode.set(nowNode);
                return true;
            }
            zkCli.delete(nowNode, -1);
        } catch (KeeperException e) {
            log.error("error_zkDistributedLock_tryLock", e);
        } catch (InterruptedException e) {
            log.error("error_zkDistributedLock_tryLock", e);
        }
        return false;
    }

    @Override
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, 0, TimeUnit.SECONDS);
    }

    @Override
    public void unlock(String lockKey) {
        if (currentLockNode.get() == null) {
            return;
        }
        try {
            zkCli.delete(currentLockNode.get(), -1);
        } catch (InterruptedException e) {
            log.error("error_zkDistributedLock_lockKey", e);
        } catch (KeeperException e) {
            log.error("error_zkDistributedLock_lockKey", e);
        }
    }
}
