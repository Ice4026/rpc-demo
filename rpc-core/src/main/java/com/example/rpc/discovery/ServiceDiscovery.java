package com.example.rpc.discovery;

import com.example.rpc.client.RpcClient;
import com.example.rpc.constant.RpcConstant;
import com.example.rpc.domain.MetaData;
import com.example.rpc.factory.ZooKeeperFactory;
import com.example.rpc.register.ServiceRegistry;
import com.example.rpc.util.HostUtil;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Xia Yubing on 2018/8/6
 */
public class ServiceDiscovery {
    // class name map to list of addresses
    private volatile ConcurrentHashMap<String, Queue<String>> addresses;
    private ZooKeeperFactory zooKeeperFactory;
    private ServiceRegistry serviceRegistry;

    public ServiceDiscovery(String registerAddress) {
        zooKeeperFactory = new ZooKeeperFactory(registerAddress);
        serviceRegistry = new ServiceRegistry(registerAddress);
        addresses = new ConcurrentHashMap<>();
    }

    public String discover(String className) {
        Queue<String> addressQueue = addresses.get(className);
        if (addressQueue == null) {
            getData(className);
            addressQueue = addresses.get(className);
            MetaData metaData = new MetaData();
            metaData.setClassName(className);
            metaData.setRole(RpcConstant.CONSUMER);
            metaData.setAddress(HostUtil.getLocalHost());

            serviceRegistry.register(metaData);
        }
        String address = addressQueue.poll();
        addressQueue.add(address);
        return address;
    }

    private void getData(String className) {
        String path = "/" + RpcConstant.REGISTRY_PATH + "/" + className + "/" + RpcConstant.PROVIDER;
        ZooKeeper zooKeeper = zooKeeperFactory.connect();
        try {
            List<String> addressList = zooKeeper.getChildren(path, event -> {
                if (Watcher.Event.EventType.NodeChildrenChanged == event.getType()) {
                    getData(className);
                }
            });
            ConcurrentLinkedQueue<String> addressQueue = new ConcurrentLinkedQueue<>(addressList);
            addresses.put(className, addressQueue);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
