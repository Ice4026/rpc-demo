package com.example.rpc.register;

import com.example.rpc.constant.RpcConstant;
import com.example.rpc.domain.MetaData;
import com.example.rpc.exception.RpcException;
import com.example.rpc.factory.ZooKeeperFactory;
import com.example.rpc.serializer.SerializationUtil;
import org.apache.zookeeper.*;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 服务注册器
 *
 * @author Xia Yubing on 2018/8/2
 */
public class ServiceRegistry {
    private ZooKeeperFactory zooKeeperFactory;

    public ServiceRegistry(String address) {
        this.zooKeeperFactory = new ZooKeeperFactory(address);
    }

    public void register(MetaData data) {
        Assert.notNull(data, "Register data cannot be null!");
        createNode(data);
    }

    private void createNode(MetaData data) {
        // /rpc-demo/serviceClass/provider|consumer/host
        new NodeBuilder(zooKeeperFactory.connect()).buildPersistent(RpcConstant.REGISTRY_PATH)
                .buildPersistent(data.getClassName())
                .buildPersistent(data.getRole())
                .buildEphemeral(data.getAddress())
                .done(data);
    }

    class NodeBuilder {
        ZooKeeper zooKeeper;
        String path = "";

        NodeBuilder(ZooKeeper zooKeeper) {
            this.zooKeeper = zooKeeper;
        }

        NodeBuilder buildPersistent(String node) {
            path = createNode(node, CreateMode.PERSISTENT);
            return this;
        }

        NodeBuilder buildEphemeral(String node) {
            path = createNode(node, CreateMode.EPHEMERAL);
            return this;
        }

        String done() {
            return path;
        }

        String done(MetaData data) {
            try {
                zooKeeper.setData(path, SerializationUtil.serialize(data), -1);
            } catch (KeeperException e) {
                throw new RpcException("Failed to create node.", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return done();
        }

        private String createNode(String node, CreateMode createMode) {
            String fullPath = path + "/" + node;
            try {
                if (zooKeeper.exists(fullPath, true) == null) {
                    fullPath = zooKeeper.create(fullPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                }
            } catch (KeeperException e) {
                throw new RpcException("Failed to create node.", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return fullPath;
        }
    }
}
