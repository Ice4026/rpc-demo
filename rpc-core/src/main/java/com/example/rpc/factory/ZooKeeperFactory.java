package com.example.rpc.factory;

import com.example.rpc.exception.RpcException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author Xia Yubing on 2018/8/6
 */
public class ZooKeeperFactory {
    private String address;

    public ZooKeeperFactory(String address) {
        this.address = address;
    }

    public ZooKeeper connect() {
        ZooKeeper zooKeeper = null;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            zooKeeper = new ZooKeeper(address, 5000, event -> {
                if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (IOException e) {
            throw new RpcException("Failed to connect to register rpc.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return zooKeeper;
    }
}
