package com.walm.lock.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
 * <p>LockWatcher</p>
 *
 * @author wangjn
 * @date 2019/6/25
 */
public class LockWatcher implements Watcher {

    private CountDownLatch latch = null;

    public LockWatcher(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

        // 删除事件 解除上一个节点的阻塞waiting状态
        if (watchedEvent.getType().equals(Event.EventType.NodeDeleted)) {
            latch.countDown();
        }
    }
}
